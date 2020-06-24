name := "sudoku-online"

version := "0.1"


libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.13" % "3.2.0" % "test",
  "com.typesafe.akka" %% "akka-http" % "10.1.12",
  "com.typesafe.akka" %% "akka-stream" % "2.5.26",
  "com.typesafe.play" %% "play-json" % "2.9.0",
  "de.heikoseeberger" %% "akka-http-circe" % "1.31.0"
)
//,"org.scalatest" %% "scalatest-flatspec" % "3.2.0" % "test"

scalaVersion := "2.13.2"
