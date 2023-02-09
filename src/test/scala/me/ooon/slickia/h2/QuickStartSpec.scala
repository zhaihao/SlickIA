/*
 * Copyright (c) 2020-2023.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.slickia.h2

import test.BaseSpec
import slick.jdbc.H2Profile.api._
import java.sql.Timestamp
import syntax.future._
/**
  * QuickStartSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2023/2/9 23:19
  */
//noinspection SqlNoDataSourceInspection,SqlResolve
class QuickStartSpec extends DBSpec {

  "now" in {
    val a = db.run(sql"select now()".as[Timestamp].head).valued
    logger.info(a.toString)
  }
}
