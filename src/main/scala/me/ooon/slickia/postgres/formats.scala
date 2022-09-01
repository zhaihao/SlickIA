
package me.ooon.slickia.postgres
import enums._
import play.api.libs.json._
import java.sql.Timestamp
// AUTO-GENERATED FILE, DO NOT MODIFY
// GENERATED TIME: Thu Sep 01 23:26:27 CST 2022
object formats {
 implicit val timestampFormat = new Format[Timestamp] {

    override def writes(t: Timestamp): JsValue = JsString(t.toString)

    override def reads(json: JsValue): JsResult[Timestamp] = json match {
      case JsNumber(d) => JsSuccess(new Timestamp(d.toLong))
      case JsString(s) =>
        scala.util.control.Exception
          .nonFatalCatch[Timestamp] opt Timestamp.valueOf(s) match {
          case Some(d) => JsSuccess(d)
          case _ =>
            JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.date.format", s))))
        }
      case _ =>
        JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.date"))))
    }
  }
  implicit val TStudentRowFormat:Format[TStudentRow] = Json.format[TStudentRow]
  implicit val TUserRowFormat:Format[TUserRow] = Json.format[TUserRow]
}

