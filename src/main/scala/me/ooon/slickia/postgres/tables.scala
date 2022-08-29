package me.ooon.slickia.postgres
// AUTO-GENERATED Slick data model
// GENERATED TIME: Wed Aug 24 22:26:50 CST 2022
/** Stand-alone Slick data model for immediate use */
import enums._
object tables extends tables {
  val profile = slick.jdbc.PGProfile
}

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait tables {
  val profile: slick.jdbc.PGProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = TCode.schema ++ TUser.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema


  /** GetResult implicit for fetching TCodeRow objects using plain SQL queries */
  implicit def GetResultTCodeRow(implicit e0: GR[Option[Int]]): GR[TCodeRow] = GR{
    prs => import prs._
    TCodeRow(<<?[Int])
  }
  /** Table description of table t_code. Objects of this class serve as prototypes for rows in queries. */
  class TCode(_tableTag: Tag) extends profile.api.Table[TCodeRow](_tableTag, "t_code") {
    /** all column projection */
    def * = id <> (TCodeRow, TCodeRow.unapply)
    /** row.map(_.??).update(row) */


    /** Database column id SqlType(serial), AutoInc */
    val id: Rep[Option[Int]] = column[Option[Int]]("id", O.AutoInc)
  }
  /** Collection-like TableQuery object for table TCode */
  lazy val TCode = new TableQuery(tag => new TCode(tag))


  /** GetResult implicit for fetching TUserRow objects using plain SQL queries */
  implicit def GetResultTUserRow(implicit e0: GR[String], e1: GR[Option[Int]], e2: GR[Option[java.sql.Timestamp]]): GR[TUserRow] = GR{
    prs => import prs._
    val r = (<<?[Int], <<[String], <<[play.api.libs.json.JsValue], <<?[play.api.libs.json.JsValue], <<?[java.sql.Timestamp], <<?[java.sql.Timestamp])
    import r._
    TUserRow.tupled((_2, _3, _4, _1, _5, _6)) // putting AutoInc last
  }
  /** Table description of table t_user. Objects of this class serve as prototypes for rows in queries. */
  class TUser(_tableTag: Tag) extends profile.api.Table[TUserRow](_tableTag, "t_user") {
    /** all column projection */
    def * = (name, conf, setting, id, createTime, updateTime) <> (TUserRow.tupled, TUserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(name), Rep.Some(conf), setting, id, createTime, Rep.Some(updateTime))).shaped.<>({r=>import r._; _1.map(_=> TUserRow.tupled((_1.get, _2.get, _3, _4, _5, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    /** row.map(_.??).update(row) */
    def ?? = (name, conf, updateTime) <> (t => {import t._; TUserRow.tupled((_1, _2, None, None, None, _3))}, (r: TUserRow) => Some(r.name, r.conf, r.updateTime))

    /** Database column name SqlType(varchar), Length(20,true) */
    val name: Rep[String] = column[String]("name", O.Length(20,varying=true))
    /** Database column conf SqlType(jsonb) */
    val conf: Rep[play.api.libs.json.JsValue] = column[play.api.libs.json.JsValue]("conf")
    /** Database column setting SqlType(jsonb), Default(None) */
    val setting: Rep[Option[play.api.libs.json.JsValue]] = column[Option[play.api.libs.json.JsValue]]("setting", O.Default(None))
    /** Database column id SqlType(serial), AutoInc */
    val id: Rep[Option[Int]] = column[Option[Int]]("id", O.AutoInc)
    /** Database column create_time SqlType(timestamp) */
    val createTime: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("create_time", O.AutoInc)
    /** Database column update_time SqlType(timestamp) */
    val updateTime: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("update_time", O.AutoInc)
  }
  /** Collection-like TableQuery object for table TUser */
  lazy val TUser = new TableQuery(tag => new TUser(tag))
}
/** Entity class storing rows of table TCode
 *  @param id Database column id SqlType(serial), AutoInc */
case class TCodeRow(id: Option[Int] = None)

/** Entity class storing rows of table TUser
 *  @param name Database column name SqlType(varchar), Length(20,true)
 *  @param conf Database column conf SqlType(jsonb)
 *  @param setting Database column setting SqlType(jsonb), Default(None)
 *  @param id Database column id SqlType(serial), AutoInc
 *  @param createTime Database column create_time SqlType(timestamp)
 *  @param updateTime Database column update_time SqlType(timestamp) */
case class TUserRow(name: String, conf: play.api.libs.json.JsValue, setting: Option[play.api.libs.json.JsValue] = None, id: Option[Int] = None, createTime: Option[java.sql.Timestamp] = None, updateTime: Option[java.sql.Timestamp] = None)
