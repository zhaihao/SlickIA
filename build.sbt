name         := "slickia"
organization := "me.ooon.slickia"
version      := "0.1.0-SNAPSHOT"
scalaVersion := "2.13.11"

libraryDependencies ++= Seq(ORISON, SQUANTS, TYPESAFE_CONFIG, POSTGRES, MYSQL, SQLITE, OS_LIB, H2)
libraryDependencies ++= Seq(SCALA_TEST, LOG, SLICK).flatten

excludeDependencies ++= Seq(
  ExclusionRule("org.slf4j", "slf4j-log4j12"),
  ExclusionRule("log4j", "log4j")
)
