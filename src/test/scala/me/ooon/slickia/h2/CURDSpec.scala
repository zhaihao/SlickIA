/*
 * Copyright (c) 2020-2023.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.slickia.h2

import slick.jdbc.GetResult
import syntax.future._

import java.sql.Timestamp

/**
  * CURDSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2023/2/9 23:12
  */
//noinspection SqlNoDataSourceInspection,SqlResolve
class CURDSpec extends DBSpec {
  import slick.jdbc.H2Profile.api._

  case class User(id: Int, name: String, age: Int, createTime: Timestamp, updateTime: Timestamp)
  implicit val GRUser: GetResult[User] = GetResult(r => User(r.<<, r.<<, r.<<, r.<<, r.<<))

  val user1 = User(1, "Tom", 10, Timestamp.valueOf("2023-01-01 01:01:01"), Timestamp.valueOf("2023-01-01 02:02:02"))
  val user2 = User(2, "Lucy", 10, Timestamp.valueOf("2023-01-01 01:01:01"), Timestamp.valueOf("2023-01-01 02:02:02"))

  val user1InsertAction = sqlu"insert into t_user values (1,'Tom',10,'2023-01-01 01:01:01','2023-01-01 02:02:02')"
  val user2InsertAction = sqlu"insert into t_user values (2,'Lucy',10,'2023-01-01 01:01:01','2023-01-01 02:02:02')"

  val user1SelectAction = sql"select * from t_user where id=1".as[User].head
  val user2SelectAction = sql"select * from t_user where id=2".as[User].head

  val user1UpdateAction = sqlu"update t_user set age=11 where id=1"

  val user1DeleteAction = sqlu"delete from t_user where id=1"

  before {
    db.run(sqlu"""
        create table t_user
        (
            id          int not null ,
            name        varchar(50) not null ,
            age         int not null,
            create_time timestamp,
            update_time timestamp
       )
    """)
      .valued

    db.run(user1InsertAction).valued
  }

  after {
    db.run(sqlu"drop table t_user").valued
  }

  "select" in {
    db.run(user1SelectAction).valued ==> user1
  }

  "insert" in {
    db.run(user2InsertAction).valued
    db.run(user2SelectAction).valued ==> user2
  }

  "update" in {
    db.run(user1UpdateAction).valued
    db.run(user1SelectAction).valued ==> user1.copy(age = 11)
  }

  "delete" in {
    db.run(user1DeleteAction).valued
    db.run(sql"select count(1) from t_user".as[Long].head).valued ==> 0
  }
}
