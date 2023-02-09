/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.slickia.postgres
import syntax.future._

/**
  * ArraySpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/9/1 21:41
  */
//noinspection SqlNoDataSourceInspection,SqlResolve
class ArraySpec extends DBSpec {

  import tables._
  import profile.api._

  val result  = TStudentRow(List(1, 2, 3), Some(List("a", "b", "c")), Some(1))
  val result2 = TStudentRow(List(1, 2, 3), Some(List("a", "b", "c")), Some(2))

  before {
    db.run(sqlu"""
        create table t_student
        (
          id    serial4 not null,
          tags  int4[] not null,
          names varchar[]
       )
    """)
      .valued
    db.run(TStudent += result).valued
  }

  after {
    db.run(sqlu"""
      drop table t_student
      """)
      .valued
  }

  "select" - {
    "api" in {
      db.run(TStudent.filter(_.id === 1).map(_.tags).result.head).valued ==> List(1, 2, 3)
      db.run(TStudent.filter(_.id === 1).result.head).valued             ==> result
    }

    "sql" in {
      db.run(sql"select tags from t_student where id=1".as[Seq[Long]]).valued.head ==> List(1, 2, 3)
      db.run(sql"select * from t_student where id=1".as[TStudentRow]).valued.head  ==> result
    }
  }

  "insert" - {
    "api" in {
      db.run(TStudent += result2).valued
      db.run(TStudent.filter(_.id === 2).result.head).valued ==> result2
    }

    "sql" in {
      db.run(sqlu"""insert into t_student(tags,names) values ('{1,2,3}','{"a","b","c"}')""").valued
      db.run(sql"select * from t_student where id=2".as[TStudentRow]).valued.head ==> result2
    }
  }
}
