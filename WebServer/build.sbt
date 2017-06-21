name := "metricsCalculatorWebServer"
organization := "giggiux"

version := "0.0.1-SNAPSHOT"

crossPaths:=false

libraryDependencies ++= Seq(
  "com.sun.net.httpserver"    % "http"               % "20070405",
  "org.postgresql"            % "postgresql"         % "42.0.0"
)

enablePlugins(DockerPlugin, JavaAppPackaging)

dockerRepository := Some("giggiux")
