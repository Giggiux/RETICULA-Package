import java.time.LocalDate
import java.util

import com.github.mauricioaniche.ck.CKNumber


object utils {
  object SQL {
    import scalikejdbc._

    Class.forName(config.sqlDriver)
    ConnectionPool.singleton(config.sqlServer + config.sqlDatabase, config.sqlUser, config.sqlPass)

    implicit val session = AutoSession

    def insertDataInDatabase(link: String, id: Int, metrics: util.Collection[CKNumber], creationDate: LocalDate, lastCommit: String, readMe: String, devNum: Int): Unit = {

      val isRepoAlreadyInDB = sql"SELECT id FROM repos where link like ${link}".map(rs => rs.long("id")).list().apply().isEmpty

      if (isRepoAlreadyInDB) {
        sql"insert into repos (link, description, classnumber, creationdate, lastcommit, devnumber) values (${link}, ${readMe}, ${metrics.size}, ${creationDate}, ${lastCommit}, ${devNum})".update.apply()

        val repoid = sql"SELECT id FROM repos where lastcommit LIKE ${lastCommit} AND link LIKE ${link}".map(rs => rs.long("id")).single.apply().get

        metrics.forEach(ckNumber => {
          sql"insert into classes (repoid, c3, cbo, lcom, ccbc, cr, loc, wmc, cd) values (${repoid}, ${ckNumber.getSpecific("C3")/1000.0}, ${ckNumber.getCbo}, ${ckNumber.getLcom}, ${ckNumber.getSpecific("CCBC")/1000.0}, ${ckNumber.getSpecific("CR")/1000.0}, ${ckNumber.getLoc}, ${ckNumber.getWmc}, ${ckNumber.getSpecific("CD")/1000.0})".update.apply()

        })
      }


    }
  }

  def getTempFolderPath(folderName : Int): String = {
    config.tempFolder + folderName
  }

}
