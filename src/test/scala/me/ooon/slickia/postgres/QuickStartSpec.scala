/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.slickia.postgres

import java.sql.Timestamp
import syntax.future._
/**
  * QuickStartSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/18 23:20
  */
class QuickStartSpec extends DBSpec {
  import slick.jdbc.PGProfile.api._
  "select now" in {
    val a = db.run(sql"select now()".as[Timestamp].head).valued
    logger.info(a.toString)
  }
}
