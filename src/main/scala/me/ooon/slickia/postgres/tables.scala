package me.ooon.slickia.postgres
// AUTO-GENERATED Slick data model
// GENERATED TIME: Thu Sep 01 23:26:26 CST 2022
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
  lazy val schema: profile.SchemaDescription = TStudent.schema ++ TUser.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema


  /** GetResult implicit for fetching TStudentRow objects using plain SQL queries */
  implicit def GetResultTStudentRow(implicit e0: GR[Option[Int]]): GR[TStudentRow] = GR{
    prs => import prs._
    val r = (<<?[Int], <<[Seq[Int]].toList, <<?[Seq[String]].map(_.toList))
    import r._
    TStudentRow.tupled((_2, _3, _1)) // putting AutoInc last
  }
  /** Table description of table t_student. Objects of this class serve as prototypes for rows in queries. */
  class TStudent(_tableTag: Tag) extends profile.api.Table[TStudentRow](_tableTag, "t_student") {
    /** all column projection */
    def * = (tags, names, id) <> (TStudentRow.tupled, TStudentRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(tags), names, id)).shaped.<>({r=>import r._; _1.map(_=> TStudentRow.tupled((_1.get, _2, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    /** row.map(_.??).update(row) */
    def ?? = tags <> (t => {TStudentRow.tupled((t, None, None))}, (r: TStudentRow) => Some(r.tags))

    /** Database column tags SqlType(_int4) */
    val tags: Rep[List[Int]] = column[List[Int]]("tags")
    /** Database column names SqlType(_varchar), Length(2147483647,false), Default(None) */
    val names: Rep[Option[List[String]]] = column[Option[List[String]]]("names", O.Length(2147483647,varying=false), O.Default(None))
    /** Database column id SqlType(serial), AutoInc */
    val id: Rep[Option[Int]] = column[Option[Int]]("id", O.AutoInc)
  }
  /** Collection-like TableQuery object for table TStudent */
  lazy val TStudent = new TableQuery(tag => new TStudent(tag))


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
    def ? = ((Rep.Some(name), Rep.Some(conf), setting, id, Rep.Some(createTime), Rep.Some(updateTime))).shaped.<>({r=>import r._; _1.map(_=> TUserRow.tupled((_1.get, _2.get, _3, _4, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    /** row.map(_.??).update(row) */
    def ?? = (name, conf, createTime, updateTime) <> (t => {import t._; TUserRow.tupled((_1, _2, None, None, _3, _4))}, (r: TUserRow) => Some(r.name, r.conf, r.createTime, r.updateTime))

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
/** Entity class storing rows of table TStudent
 *  @param tags Database column tags SqlType(_int4)
 *  @param names Database column names SqlType(_varchar), Length(2147483647,false), Default(None)
 *  @param id Database column id SqlType(serial), AutoInc */
case class TStudentRow(tags: List[Int], names: Option[List[String]] = None, id: Option[Int] = None)

/** Entity class storing rows of table TUser
 *  @param name Database column name SqlType(varchar), Length(20,true)
 *  @param conf Database column conf SqlType(jsonb)
 *  @param setting Database column setting SqlType(jsonb), Default(None)
 *  @param id Database column id SqlType(serial), AutoInc
 *  @param createTime Database column create_time SqlType(timestamp)
 *  @param updateTime Database column update_time SqlType(timestamp) */
case class TUserRow(name: String, conf: play.api.libs.json.JsValue, setting: Option[play.api.libs.json.JsValue] = None, id: Option[Int] = None, createTime: Option[java.sql.Timestamp] = None, updateTime: Option[java.sql.Timestamp] = None)
