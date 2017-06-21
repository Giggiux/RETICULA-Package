name := "githubMC"
organization := "giggiux"

version := "1.0"

scalaVersion := "2.12.1"

val nexusRepository = "https://rio.inf.usi.ch/nexus/repository/maven-student/"

resolvers += "Sonatype Nexus Repository Manager" at nexusRepository

val sbtCredentialsFile = Path.userHome / ".sbt" / ".credentials"

if(sbtCredentialsFile.exists())
  credentials += Credentials(sbtCredentialsFile)
else
  credentials += Credentials("Sonatype Nexus Repository Manager", "rio.inf.usi.ch", System.getenv("NEXUS_USER"), System.getenv("NEXUS_PASSWORD"))

publishMavenStyle:=true
publishTo:={
  val releaseOrSnapshot = if (isSnapshot.value) "snapshots" else "releases"
  Some(releaseOrSnapshot at nexusRepository)
}

lazy val akkaVersion        = "2.4.17"
lazy val json4sVersion      = "3.5.1"
lazy val scalikeJDBCVersion = "2.5.+"
lazy val postgreSQLVersion  = "42.0.0"
lazy val jgitVersion        = "4.6.+"
lazy val MCVersion          = "0.0.3-SNAPSHOT"
lazy val scalaJVersion      = "2.3.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka"       %% "akka-actor"         % akkaVersion,
  "org.json4s"              %% "json4s-native"      % json4sVersion,
  "org.scalikejdbc"         %% "scalikejdbc"        % scalikeJDBCVersion,
  "org.scalaj"              %% "scalaj-http"        % scalaJVersion,
  "org.postgresql"           % "postgresql"         % postgreSQLVersion,
  "org.eclipse.jgit"         % "org.eclipse.jgit"   % jgitVersion,
  "giggiux"                  % "metricscalculator"  % MCVersion
)


enablePlugins(DockerPlugin, JavaAppPackaging)

dockerRepository := Some("giggiux")