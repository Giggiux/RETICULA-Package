name := "metricsCalculator"
organization := "giggiux"

version := "0.0.4-SNAPSHOT"

crossPaths:=false

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

libraryDependencies ++= Seq(
  "org.apache.commons"        % "commons-math3"           % "3.3",
  "org.apache.lucene"         % "lucene-core"             % "6.4.2",
  "org.apache.lucene"         % "lucene-analyzers-common" % "6.4.2",
  "com.google.guava"          % "guava"                   % "21.0",
  "com.github.mauricioaniche" % "ck"                      % "0.2.0",
  "raykernel.apps"            % "readability"             % "1.0"
)
