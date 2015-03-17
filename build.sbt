name := """neptunes-pride-api"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.scalatest" % "scalatest_2.11" % "2.2.4"
)
