/*
 * Copyright (c) 2020-2023.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.slickia.h2

import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import test.BaseSpec

import scala.concurrent.duration.DurationInt

/**
  * DBSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2023/2/9 23:11
  */
trait DBSpec extends BaseSpec with StrictLogging with BeforeAndAfterAll with BeforeAndAfter {
  import slick.jdbc.H2Profile.api._
  implicit val TIMEOUT = 5.seconds

  var db: Database = _
  override def beforeAll(): Unit = { db = Database.forConfig("test.h2") }
  override def afterAll():  Unit = { db.close() }
}
