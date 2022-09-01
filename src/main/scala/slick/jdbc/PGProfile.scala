
package slick.jdbc

import com.github.tminglei.slickpg._
import play.api.libs.json.{JsValue, Json}
import slick.basic.Capability
import me.ooon.slickia.postgres.enums._

// AUTO-GENERATED FILE, DO NOT MODIFY
// GENERATED TIME: Thu Sep 01 23:26:27 CST 2022
trait PGProfile
    extends ExPostgresProfile
    with PgArraySupport
    with PgEnumSupport
    with PgDate2Support
    with PgRangeSupport
    with PgHStoreSupport
    with PgPlayJsonSupport
    with PgSearchSupport
    with PgPostGISSupport
    with PgNetSupport
    with PgLTreeSupport {

  // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"
  def pgjson = "jsonb"

  // pg
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + slick.jdbc.JdbcCapabilities.insertOrUpdate

  override val api = new API
        with ByteaPlainImplicits
        with SimpleArrayPlainImplicits
        with Date2DateTimePlainImplicits
        with PlayJsonPlainImplicits
        with SimpleNetPlainImplicits
        with SimpleLTreePlainImplicits
        with SimpleRangePlainImplicits
        with SimpleHStorePlainImplicits
        with SimpleSearchPlainImplicits {}

  trait API
      extends super.API
      with ArrayImplicits
      with DateTimeImplicits
      with JsonImplicits
      with NetImplicits
      with LTreeImplicits
      with RangeImplicits
      with HStoreImplicits
      with SearchImplicits
      with SearchAssistants {
    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
    implicit val playJsonArrayTypeMapper =
      new AdvancedArrayJdbcType[JsValue](
        pgjson,
        s => utils.SimpleArrayUtils.fromString[JsValue](Json.parse)(s).orNull,
        v => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)
      ).to(_.toList)
    
  }
}

object PGProfile extends PGProfile
