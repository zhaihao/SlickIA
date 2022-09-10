/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */
import sbt._

/**
  * Dependencies
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/18 19:58
  */
object Dependencies extends AutoPlugin {
  override def requires = empty
  override def trigger  = allRequirements

  object autoImport {

    lazy val ORISON          = "me.ooon"                %% "orison"                        % "1.0.2"
    lazy val NSCALA          = "com.github.nscala-time" %% "nscala-time"                   % "2.30.0"
    lazy val OS_LIB          = "com.lihaoyi"            %% "os-lib"                        % "0.8.1"
    lazy val SQUANTS         = "org.typelevel"          %% "squants"                       % "1.7.4"
    lazy val TYPESAFE_CONFIG = "com.typesafe"            % "config"                        % "1.4.2"
    lazy val PLAY_JSON       = "com.typesafe.play"      %% "play-json"                     % "2.9.2"
    lazy val SQLITE          = "org.xerial"              % "sqlite-jdbc"                   % "3.39.3.0"
    lazy val PAR             = "org.scala-lang.modules" %% "scala-parallel-collections"    % "1.0.4"
    lazy val CLICKHOUSE      = "com.github.housepower"   % "clickhouse-native-jdbc-shaded" % "2.6.5"
    lazy val HIKARI          = "com.zaxxer"              % "HikariCP"                      % "5.0.1"
    lazy val POSTGRES        = "org.postgresql"          % "postgresql"                    % "42.5.0"
    lazy val MYSQL           = "mysql"                   % "mysql-connector-java"          % "8.0.30"

    lazy val DOCKER = Seq(
      "com.whisk"  %% "docker-testkit-scalatest" % "0.11.0",
      "com.spotify" % "docker-client"            % "8.16.1"
    )

    lazy val SLICK = Seq(
      "com.typesafe.slick"  %% "slick"              % "3.4.0",
      "com.github.tminglei" %% "slick-pg"           % "0.21.0",
      "com.github.tminglei" %% "slick-pg_play-json" % "0.21.0",
      "com.github.tminglei" %% "slick-pg_jts_lt"    % "0.21.0",
      "com.typesafe.slick"  %% "slick-hikaricp"     % "3.4.0"
    )

    lazy val LOG = Seq(
      "org.slf4j"                   % "log4j-over-slf4j" % "2.0.0",
      "com.typesafe.scala-logging" %% "scala-logging"    % "3.9.5",
      "ch.qos.logback"              % "logback-classic"  % "1.4.0"
    )

    lazy val SCALA_TEST = Seq(
      "org.scalatest" %% "scalatest-core"           % "3.2.13",
      "org.scalatest"  % "scalatest-compatible"     % "3.2.13",
      "org.scalatest" %% "scalatest-diagrams"       % "3.2.13",
      "org.scalatest" %% "scalatest-matchers-core"  % "3.2.13",
      "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.13",
      "org.scalatest" %% "scalatest-freespec"       % "3.2.13"
    )
  }
}
