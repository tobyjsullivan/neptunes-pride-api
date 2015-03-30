name := """neptunes-pride-api"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  cache,
  ws,
  jdbc,
  anorm,
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "org.scalatest" % "scalatest_2.11" % "2.2.4"
)
