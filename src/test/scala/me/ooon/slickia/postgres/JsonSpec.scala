/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.slickia.postgres

import play.api.libs.json._
import syntax.future._

/**
  * JsonSpec
  *
  * [[https://github.com/tminglei/slick-pg/blob/master/addons/play-json/src/test/scala/com/github/tminglei/slickpg/PgPlayJsonSupportSuite.scala]]
  *
  * [[https://github.com/tminglei/slick-pg/tree/master/core/src/main/scala/com/github/tminglei/slickpg/json]]
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/18 21:24
  */
class JsonSpec extends DBSpec {

  import tables._
  import profile.api._

  val json = Json.obj("a" -> "av", "b" -> 1, "c" -> List(1, 2, 3), "d" -> Json.obj("d1" -> "dv", "d2" -> List("av", "bv", "cv")))
  before {
    db.run(sqlu"""
          create table t_user
          (
              id          serial4                             not null,
              name        varchar(20)                         not null,
              conf        jsonb                               not null,
              setting     jsonb,
              create_time timestamp default current_timestamp not null,
              update_time timestamp default current_timestamp not null
          )
      """)
      .valued
    val a = TUser += TUserRow("tom", json, Some(json))
    db.run(a).valued
  }

  after {
    db.run(sqlu"""
        drop table t_user
        """)
      .valued
  }

  "insert" - {
    "api" in {
      val a = TUser += TUserRow("tom", json)
      db.run(a).valued
      db.run(TUser.filter(_.id === 2).result.head).valued.conf ==> json
    }

    "sql" in {
      db.run(sqlu"""insert into t_user(name, conf) values ('tom','{"a":"av","b":1,"c":[1,2,3],"d":{"d1":"dv","d2":["av","bv","cv"]}}')""").valued
      db.run(TUser.filter(_.id === 2).result.head).valued.conf ==> json
    }
  }

  "select" - {
    "api" in {
      val action = TUser.filter(_.id === 1).result.head
      db.run(action).valued.conf ==> json
    }

    "sql" in {
      val action = sql"select * from t_user where id=1".as[TUserRow].head
      db.run(action).valued.conf ==> json
    }
  }

  "delete" - {
    "api" in {
      val action = TUser.filter(_.id === 1).delete
      db.run(action).valued
      db.run(TUser.size.result).valued ==> 0
    }

    "sql" in {
      val action = sqlu"delete from t_user where id=1"
      db.run(action).valued
      db.run(TUser.size.result).valued ==> 0
    }
  }

  "update" - {
    "api" in {
      val action = TUser.filter(_.id === 1).map(_.name).update("tom1")
      db.run(action).valued
      db.run(TUser.result.head).valued.name ==> "tom1"
    }

    "sql" in {
      val action = sqlu"update t_user set name='tom1' where id=1"
      db.run(action).valued
      db.run(TUser.result.head).valued.name ==> "tom1"
    }
  }

  "json 操作" - {
    "sql 原生语法插入" in {
      db.run(sqlu"""insert into t_user(name, conf) values ('tom','{"a":"av","b":1,"c":[1,2,3],"d":{"d1":"dv","d2":["av","bv","cv"]}}'::jsonb)""")
        .valued
      db.run(TUser.filter(_.id === 1).result.head).valued.conf ==> json
    }

    "获取 key" - {
      "返回 json" - {
        "sql" in {
          val action = sql"select conf -> 'b' from t_user where id=1".as[JsValue].head
          db.run(action).valued ==> JsNumber(1)
        }

        // option 的json，返回option
        "api" in {
          val action = TUser.filter(_.id === 1).map(_.setting +> "b").result.head
          db.run(action).valued ==> Some(JsNumber(1))
        }
      }

      "返回 string" - {
        "sql" in {
          val action = sql"select conf ->> 'b' from t_user where id=1".as[String].head
          db.run(action).valued ==> "1"
        }

        "api" in {
          val action = TUser.filter(_.id === 1).map(_.conf +>> "b").result.head
          db.run(action).valued ==> "1"
        }
      }

    }

    "获取数组的元素" - {
      "返回 json" - {
        "sql" in {
          val action = sql"select conf -> 'c' -> 0 from t_user where id=1".as[JsValue].head
          db.run(action).valued ==> JsNumber(1)
        }

        "api" in {
          val action = TUser.filter(_.id === 1).map(u => (u.conf +> "c") ~> 0).result.head
          db.run(action).valued ==> JsNumber(1)
        }
      }

      "返回 string" - {
        "sql" in {
          val action = sql"select conf -> 'c' ->> 0 from t_user where id=1".as[String].head
          db.run(action).valued ==> "1"
        }

        "api" in {
          val action = TUser.filter(_.id === 1).map(u => (u.conf +> "c") ~>> 0).result.head
          db.run(action).valued ==> "1"
        }
      }
    }

    "获取path" - {
      "返回 json" - {
        "sql" in {
          val action1 = sql"select conf #>'{c,0}' from t_user where id=1".as[JsValue].head
          db.run(action1).valued ==> JsNumber(1)
          val action2 = sql"select conf #>'{d,d2,0}' from t_user where id=1".as[JsValue].head
          db.run(action2).valued ==> JsString("av")
        }

        "api" in {
          val action1 = TUser.filter(_.id === 1).map(_.conf #> List("c", "0")).result.head
          db.run(action1).valued ==> JsNumber(1)
          val action2 = TUser.filter(_.id === 1).map(_.conf #> List("d", "d2", "0")).result.head
          db.run(action2).valued ==> JsString("av")
        }
      }

      "返回 String" - {
        "sql" in {
          val action1 = sql"select conf #>>'{c,0}' from t_user where id=1".as[String].head
          db.run(action1).valued ==> "1"
          val action2 = sql"select conf #>>'{d,d2,0}' from t_user where id=1".as[String].head
          db.run(action2).valued ==> "av"
        }

        "api" in {
          val action1 = TUser.filter(_.id === 1).map(_.conf #>> List("c", "0")).result.head
          db.run(action1).valued ==> "1"
          val action2 = TUser.filter(_.id === 1).map(_.conf #>> List("d", "d2", "0")).result.head
          db.run(action2).valued ==> "av"
        }
      }
    }
    // 对于 array 直接合并，对于object，不同key 进行合并，同key后面覆盖前面
    "json 合并" - {
      "sql" in {
        db.run(sql"""select '[1,2]'::jsonb || '[2,3]'::jsonb""".as[JsValue].head).valued           ==> Json.arr(1, 2, 2, 3)
        db.run(sql"""select '{"a":"av"}'::jsonb || '{"b":"bv"}'::jsonb""".as[JsValue].head).valued ==> Json.obj("a" -> "av", "b" -> "bv")
        db.run(
          sql"""
            select (conf -> 'a') || (setting -> 'a') from t_user
            """.as[JsValue].head
        ).valued ==> Json.arr("av", "av")

        db.run(
          sql"""
            select conf || setting from t_user
            """.as[JsValue].head
        ).valued ==> json
      }

      "api" in {
        db.run(
          TUser
            .filter(_.id === 1)
            .map(r => {
              (r.conf +> "a") || (r.setting +> "a")
            })
            .result
            .head
        ).valued ==> Some(Json.arr("av", "av"))
      }
    }

    "删除 key" - {
      "sql" in {
        db.run(sql"select conf - 'a' from t_user".as[JsValue].head).valued ==> json - "a"
      }

      "api" in {
        db.run(TUser.map(_.conf - "a").result.head).valued ==> json - "a"
      }
    }

    "删除元素" - {
      "sql" in {
        db.run(sql"select (conf -> 'c') - 1 from t_user".as[JsValue].head).valued ==> Json.arr(1, 3)
      }
    }
  }
}
