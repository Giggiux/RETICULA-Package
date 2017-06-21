import scala.concurrent.duration._

object config {

  val maxMetricsCalculationTime : Duration = 60 minutes

  val actors = 5

  val gits = 30000

  val gitsList : String = sys.env("GITS_LIST")

  val readMeLengthMin = 250

  val sqlDriver = "org.postgresql.Driver"
  val sqlServer : String = sys.env("SQL_SERVER")
  val sqlDatabase : String = sys.env("SQL_DB")
  val sqlUser : String = sys.env("SQL_USER")
  val sqlPass : String = sys.env("SQL_PASSWORD")

  val tempFolder = "./tempFolder/"
}
