import java.io.File
import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import java.util

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import com.github.mauricioaniche.ck.CKNumber
import it.frunzioluigi.metricsCalculator.ReducedCK
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.revwalk.{RevSort, RevWalk}
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.concurrent.{Await, Future}
import scala.sys.process._
import scala.util.Try

// Messages
case class GitRepoLinkMsg(link: String, idx: Int /* to avoid same name's repo */)

case class RepoIsNotValid(message: String)

case class RepoIsValid()

case class StartingMessage()

case class EndingMessage()

class GitsDispatcherActor(listOfGitLinks: List[String]) extends Actor with akka.actor.ActorLogging {
  private var repoLinks = scala.util.Random.shuffle(listOfGitLinks)
  private var validRepos = 0
  private var messagesReceived = 0
  private var messagesSent = 0

  private var actors = List[ActorRef]()
  private var fileSender: Option[ActorRef] = None

  def receive = {
    case StartingMessage() => {
      log.info(s"Starting system with ${repoLinks.length} repositories in JSON file")
      fileSender = Some(sender)

      actors = (1 to config.actors).toList.map(id => context.system.actorOf(Props(new GitsMetricsCalculatorActor), s"metrics-calculator-$id"))

      val firstMessages = repoLinks.take(config.actors)
      repoLinks = repoLinks.drop(config.actors)

      for ((link, actor) <- firstMessages zip actors) {
        actor ! GitRepoLinkMsg(link, messagesSent)
        messagesSent += 1
      }
    }
    case RepoIsValid() => {
      validRepos += 1
      sendNewLink(sender)
      log.info(s"received a RepoIsValid message")
    }
    case RepoIsNotValid(message) => {
      sendNewLink(sender)
      log.info(s"received a RepoIsNotValid message: $message")
    }
    case x => {
      log.debug(s"GitsDispatcher got unknown message: " + x.getClass)
    }
  }

  def sendNewLink(actor: ActorRef): Unit = {
    log.info(s"Valid Repos are $validRepos, remaining repositories in JSON are ${repoLinks.length},\n messages sent $messagesSent, messages received $messagesReceived")
    messagesReceived += 1
    val allActorsFinished = messagesReceived == messagesSent

    if ((validRepos >= config.gits || repoLinks.isEmpty) && allActorsFinished) {
      fileSender.map(_ ! EndingMessage())
    } else if (validRepos <= config.gits && !repoLinks.isEmpty) {
      val head = repoLinks.head
      messagesSent += 1
      actor ! GitRepoLinkMsg(head, messagesSent)
      repoLinks = repoLinks.tail
    }
  }

}

class GitsMetricsCalculatorActor extends Actor with akka.actor.ActorLogging {

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  def receive = {
    case GitRepoLinkMsg(link, folderName) => {

      val mySender = sender

      log.info(s"receiving message for " + link)

      val readMeSizeIsValidOptionFuture = cloneGitRepo(link, folderName).map { gitCloneExecutedCorrectly: Boolean =>
        if (gitCloneExecutedCorrectly) {
          Some(getReadMeSizeIsValid(folderName))
        } else {
          mySender ! RepoIsNotValid(s"$link is a NOT valid repo: cloning didn't go well")
          None
        }
      }

      val metricsFuture = readMeSizeIsValidOptionFuture.map { readMeSizeIsValidOption: Option[(String, Boolean)] =>
        readMeSizeIsValidOption.map { case (readMe, readMeSizeIsValid) =>
          if (readMeSizeIsValid) {
            Some((readMe, computeMetrics(folderName)))
          } else {
            mySender ! RepoIsNotValid(s"$link is a NOT valid repo: ReadMe too short")
            None
          }
        }
      }

      metricsFuture.map { metricsOption =>
        metricsOption.foreach { case Some((readMe, metrics)) =>
          if (metrics.size > 0) {
            val creationDate = getCreationDay(folderName)
            val lastCommit = getLastCommit(folderName)
            val devNum = Try(getNumOfDeveloper(link)) getOrElse 0
            utils.SQL.insertDataInDatabase(link, folderName, metrics, creationDate, lastCommit, readMe, devNum)
            mySender ! RepoIsValid()
          } else {
            mySender ! RepoIsNotValid(s"$link is a NOT valid repo: no classes found")
          }
          removeRepoFolder(folderName)
        case None =>
          removeRepoFolder(folderName)
        }
      }
    }

    case x => {
      log.debug(s"GitsMetricsCalculator received wrong message: " + x.getClass)
    }
  }

  private def getLastCommit(folderName: Int): String = {
    val git = new FileRepository(
      new File(utils.getTempFolderPath(folderName) + "/.git"))

    val rw = new RevWalk(git)
    val headId = git.resolve(Constants.HEAD)
    val root = rw.parseCommit(headId)
    rw.markStart(root)
    val commit = rw.next().name()
    git.close()
    commit
  }

  private def getCreationDay(folderName: Int): LocalDate = {

    val git = new FileRepository(
      new File(utils.getTempFolderPath(folderName) + "/.git"))

    val rw = new RevWalk(git)
    val headId = git.resolve(Constants.HEAD)
    val root = rw.parseCommit(headId)
    rw.sort(RevSort.REVERSE)
    rw.markStart(root)
    val commit = rw.next()
    val date = LocalDateTime.ofEpochSecond(commit.getCommitTime, 0, ZoneOffset.UTC).toLocalDate
    git.close()
    date
  }

  private def cloneGitRepo(link: String, folderName: Int): Future[Boolean] = {
    val p = s"git clone $link ${utils.getTempFolderPath(folderName)}".run()
    val futureExitValue: Future[Int] = Future {
      p.exitValue()
    }
    futureExitValue.map { exitCode => exitCode == 0 }
  }

  private def getReadMeSizeIsValid(folderName: Int): (String, Boolean) = {
    import java.nio.file.{Files, Paths}

    val pathString = s"${utils.getTempFolderPath(folderName)}/README.md"
    val fileExists = Files.exists(Paths.get(pathString))
    val readMe = Try {
      scala.io.Source.fromFile(pathString).mkString
    }.getOrElse("")
    val readMeSize = readMe.length
    (readMe, readMeSize >= config.readMeLengthMin)
  }

  private def computeMetrics(folderName: Int): util.Collection[CKNumber] = {
    val myCK = new ReducedCK()
    val reportFuture = Future(myCK.calculate(utils.getTempFolderPath(folderName)))
    Try {
      Await.result(reportFuture, config.maxMetricsCalculationTime).all()
    } getOrElse new util.ArrayList[CKNumber]()
  }

  private def removeRepoFolder(folderName: Int): Unit = {
    s"rm -rf ${utils.getTempFolderPath(folderName)}".!
  }

  private def getNumOfDeveloper(link: String): Int = {
    val owner_repo = link.drop(19).dropRight(4)

    val newLink = s"https://api.github.com/repos/${owner_repo}/contributors"

    import scalaj.http._

    val headers = Seq(
      ("User-Agent", "javaReposCrawler"),
      ("Authorization", "token 8a51af9ea47c1b6affd55b64bf17efea46134c0d"))

    val response = Http(newLink).param("per_page", "100").headers(headers).asString

    val parsed = parse(response.body)

    val contributors = for (JArray(contributors) <- parsed) yield contributors

    contributors(0).length
  }

}


object Main extends App {

  import akka.pattern.ask

  import scala.concurrent.duration._


  override def main(args: Array[String]) {
    implicit val ec = akka.dispatch.ExecutionContexts.global

    val system = ActorSystem("GitSystem")

    val source = scala.io.Source.fromFile(config.gitsList)
    val parsed = parse(source.mkString)

    val git: List[String] = for {
      JArray(gits) <- parsed
      JString(git) <- gits
    } yield git

    val actor = system.actorOf(Props(new GitsDispatcherActor(git)))

    implicit val timeout = Timeout(21474835 seconds)
    val future = actor ? StartingMessage()

    println(Await.result(future, Duration.Inf))
    system.terminate()
  }
}
