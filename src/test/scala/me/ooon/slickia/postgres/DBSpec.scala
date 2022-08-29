/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.slickia.postgres

import com.typesafe.scalalogging.StrictLogging
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import test.BaseSpec

import scala.concurrent.duration.DurationInt

/**
  * DBSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/18 21:40
  */
trait DBSpec extends BaseSpec with StrictLogging with BeforeAndAfterAll with BeforeAndAfter {
  import slick.jdbc.PGProfile.api._
  implicit val TIMEOUT = 5.seconds

  var db: Database = _
  override def beforeAll(): Unit = { db = Database.forConfig("test") }
  override def afterAll():  Unit = { db.close() }
}