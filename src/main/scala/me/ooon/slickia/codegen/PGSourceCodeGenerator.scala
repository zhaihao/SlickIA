/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.slickia.codegen

import closeable.using
import com.typesafe.scalalogging.StrictLogging
import me.ooon.slickia.PGCodeGen
import slick.model.Model
import slick.sql.SqlProfile.ColumnOption

import java.sql.DriverManager
import java.util.Date
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/** PGSourceCodeGenerator
  *
  * @author
  *   zhaihao
  * @version 1.0
  * @since 2021/4/29
  *   14:31
  */
//noinspection NotImplementedCode
class PGSourceCodeGenerator(model: Model) extends SourceCodeGenerator(model) with StrictLogging {
  val models = ArrayBuffer.empty[String]

  override def Table = new Table(_) {
    val genColumn = PGCodeGen.autoGenColumns
    def isGen(model: slick.model.Column) = genColumn.contains(model.name.toLowerCase)
    model.columns.foreach(c => {
      logger.debug(model.name.table +": " + c)
    })
    override def autoIncLast = true
    override def TableClass = new TableClass {
      // 增加 ??
      def nonUpdate = {
        val cls = columns.filter(!_.model.nullable)

        def tupledExpr = {
          val len = cls.length
          var j = 0
          val tupExpr = columns.indices
            .map { i =>
              val c = columns(i)
              if (!c.model.nullable) {
                j = j + 1
                if (len == 1) "t" else s"_$j"
              } else {
                "None"
              }
            }
            .mkString(", ")

          if (len > 1) s"t => {import t._; ${TableClass.elementType}.tupled(($tupExpr))}"
          else s"t => {${TableClass.elementType}.tupled(($tupExpr))}"
        }

        def unapplyExpr = {
          val paramsExpr = cls.map(c => s"r.${c.name}").mkString(", ")
          s"(r: ${TableClass.elementType}) => Some($paramsExpr)"
        }

        if (cls.isEmpty) {
          ""
        } else {
          val struct = compoundValue(cls.map(c => if (c.asOption) s"Rep.Some(${c.name})" else s"${c.name}"))
          val rhs = if (mappingEnabled) {
            s"$struct <> ($tupledExpr, $unapplyExpr)"
          } else struct

          s"""def ?? = $rhs""".stripMargin
        }
      }
      override def definitions = {
        def OptionDef = new Def {
          def doc              = "Maps whole row to an option. Useful for outer joins."
          override def enabled = optionEnabled
          def code             = option
          def rawName          = ???
        }
        def StarDef = new Def {
          def doc     = "all column projection"
          def code    = star
          def rawName = ???
        }
        def NonUpdateDef = new Def {
          def doc     = "row.map(_.??).update(row)"
          def code    = nonUpdate
          def rawName = ???
        }

        Seq[Seq[Def]](
          Seq(StarDef, OptionDef, NonUpdateDef),
          columns,
          primaryKey.toSeq,
          foreignKeys,
          indices
        )
      }
    }

    // null 字段 case class 给None默认值
    override def EntityType = new EntityType {

      override def docWithCode: String = {
        models += super.docWithCode + "\n"
        ""
      }

      override def code = {
        val args = columns
          .map(c =>
            c.default
              .map(v => s"${c.name}: ${c.exposedType} = $v")
              .getOrElse(
                if (c.model.nullable || isGen(c.model) || c.autoInc) s"${c.name}: Option[${c.rawType}] = None"
                else s"${c.name}: ${c.exposedType}"
              )
          )
          .mkString(", ")
        if (classEnabled) {
          val prns = (parents.take(1).map(" extends " + _) ++ parents.drop(1).map(" with " + _)).mkString("")
          (if (caseClassFinal) "final " else "") +
            s"""case class $name($args)$prns"""
        } else {
          if (columns.size > 254)
            s"type $name = $types" // constructor method would exceed JVM parameter limit
          else
            s"""
type $name = $types
/** Constructor for $name providing default values if available in the database schema. */
def $name($args): $name = {
  ${compoundValue(columns.map(_.name))}
}
          """.trim
        }
      }
    }

    override def PlainSqlMapper = new PlainSqlMapper {
      override def code = {
        val types = columnsPositional.map(c => if (c.asOption || c.model.nullable || isGen(c.model) || c.autoInc) s"<<?[${c.rawType}]" else s"<<[${c.rawType}]")
        val dependencies = columns
          .map(_.exposedType)
          .distinct
          .filter(!_.contains("JsValue"))
          .zipWithIndex
          .map { case (t, i) => s"""e$i: GR[$t]""" }
          .mkString(", ")

        def result(args: String) = if (mappingEnabled) s"$factory($args)" else args

        val body =
          if (autoIncLast && columns.size > 1) {
            val rearranged = {
              val r = desiredColumnOrder.map(i => if (hlistEnabled || isMappedToHugeClass) s"r($i)" else tuple(i))
              if (isMappedToHugeClass) r.mkString(", ") else compoundValue(r)
            }
            s"""
val r = ${compoundValue(types)}
import r._
${result(rearranged)} // putting AutoInc last
            """.trim
          } else
            result(if (isMappedToHugeClass) types.mkString(", ") else compoundValue(types))

        s"""
implicit def ${name}(implicit $dependencies): GR[${TableClass.elementType}] = GR{
  prs => import prs._
  ${indent(body)}
}
        """.trim
      }
    }

    override def Column = new Column(_) {

      override def code: String = {
        val opts = if(isGen(model)) options.toSeq :+  "O.AutoInc" else options
        s"""val $name: Rep[$actualType] = column[$actualType]("${model.name}"${opts.map(", " + _).mkString("")})"""
      }

      override def actualType: String = {
        if (model.nullable || isGen(model) || autoInc) {
          optionType(rawType)
        } else rawType
      }
      override def autoInc: Boolean = {
        if (isGen(model)) {
          true
        } else {
          model.options.contains(slick.ast.ColumnOption.AutoInc)
        }
      }

      // 类型映射
      override def rawType: String = {
        model.options
          .filter(_.isInstanceOf[ColumnOption.SqlType])
          .head
          .asInstanceOf[ColumnOption.SqlType]
          .typeName match {
          case "hstore"                                      => "Map[String, String]"
          case "_text" | "text[]" | "_varchar" | "varchar[]" => "List[String]"
          case "geometry"                                    => "com.locationtech.jts.geom.Geometry"
          case "_int8" | "int8[]"                            => "List[Long]"
          case "_int4" | "int4[]"                            => "List[Int]"
          case "_int2" | "int2[]"                            => "List[Short]"
          case "int2"                                        => "Short"
          case "int4"                                        => "Int"
          case "int8"                                        => "Long"
          case "serial"                                      => "Int"
          case "bigserial"                                   => "Long"
          case "bool"                                        => "Boolean"
          case "varchar"                                     => "String"
          case "text"                                        => "String"
          case "timestamp"                                   => "java.sql.Timestamp"
          case "json" | "jsonb" | "jsonpath"                 => "play.api.libs.json.JsValue"
          case enum if model.tpe == "String" =>
            s"${PGCodeHelper.snakeToCamel(PGCodeHelper.removeTypePostfix(enum))}.Value"
          case _ => throw new Exception(s"无法处理的类型映射，model:$model")
        }
      }
    }

  }

  def outsideCode = s"${models.mkString("\n")}"

  override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]) = {
    s"""
package $pkg
// AUTO-GENERATED Slick data model
// GENERATED TIME: ${new Date()}
/** Stand-alone Slick data model for immediate use */
import enums._
object $container extends $container {
  val profile = slick.jdbc.PGProfile
}

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait $container${parentType.map(t => s" extends $t").getOrElse("")} {
  val profile: slick.jdbc.PGProfile
  import profile.api._
  ${indent(code)}
}
$outsideCode
      """.trim()
  }

}
object PGFormatCodeGenerator {
  import PGCodeHelper._
  def gen(url: String, user: String, password: String, pkg: String, model: Model, schema: String) = {
    val enumInfo = PGCodeHelper.gen(url, user, password, schema)
    val enumFormats: Seq[String] = enumInfo.view.keys
      .map(k => {
        val enumName = snakeToCamel(k)
        s"  implicit val ${enumName}Format:Format[$enumName.Value] = Json.formatEnum($enumName)"
      })
      .toSeq

    val entityFormats = model.tables.map(t => {
      val entity = toCamelCase(t.name.table) + "Row"
      s"  implicit val ${entity}Format:Format[$entity] = Json.format[$entity]"
    })
    makeSourceFileContent(pkg, enumFormats ++: entityFormats)
  }

  def toCamelCase(str: String): String = str.toLowerCase
    .split("_")
    .map {
      case "" => "_"
      case s  => s
    } // avoid possible collisions caused by multiple '_'
    .map(_.capitalize)
    .mkString("")

  private def makeSourceFileContent(pkg: String, items: Seq[String]): String = {
    s"""
package $pkg
import enums._
import play.api.libs.json._
import java.sql.Timestamp
// AUTO-GENERATED FILE, DO NOT MODIFY
// GENERATED TIME: ${new Date()}
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
${items.mkString("\n")}
}

""".stripMargin
  }

}
object PGProfileCodeGenerator {
  import PGCodeHelper._
  def gen(url: String, user: String, password: String, pkg: String, schema: String) = {
    val enumInfo = PGCodeHelper.gen(url, user, password, schema)
    val enumsCode = enumInfo.view
      .mapValues(_.sortBy(_._2).map(_._1))
      .toSeq
      .map(en => {
        val label = snakeToCamel(removeTypePostfix(en._1))
        (en._1, label, formatEnumeration(label, en._2))
      })

    val enumImplicits = enumsCode.map(t => toSingleEnumImplicits(t._1, t._2))
    makeSourceFileContent(pkg, enumImplicits)
  }

  def toSingleEnumImplicits(originalName: String, enumClass: String): String = {
    val className = enumNameToScalaClassName(enumClass)
    s"""
       |    // mapping for $originalName / $className
       |    implicit val ${className}TypeMapper = createEnumJdbcType("$originalName", $className)
       |    implicit val ${className}ListTypeMapper = createEnumListJdbcType("${enumNameToListName(originalName)}", $className)
       |    implicit val ${className}ColumnExtensionMethodsBuilder = createEnumColumnExtensionMethodsBuilder($className)
       |    implicit val ${className}OptionColumnExtensionMethodsBuilder = createEnumOptionColumnExtensionMethodsBuilder($className)
         """.stripMargin
  }

  private def makeSourceFileContent(pkg: String, items: Seq[String]): String = {
    s"""
package slick.jdbc

import com.github.tminglei.slickpg._
import play.api.libs.json.{JsValue, Json}
import slick.basic.Capability
import $pkg.enums._

// AUTO-GENERATED FILE, DO NOT MODIFY
// GENERATED TIME: ${new Date()}
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
    ${items.mkString("\n")}
  }
}

object PGProfile extends PGProfile
""".stripMargin
  }
}

object PGEnumCodeGenerator {
  import PGCodeHelper._
  def gen(url: String, user: String, password: String, pkg: String, schema: String) = {
    val enumInfo = PGCodeHelper.gen(url, user, password, schema)
    val enumsCode = enumInfo.view
      .mapValues(_.sortBy(_._2).map(_._1))
      .toSeq
      .map(en => {
        val label = snakeToCamel(removeTypePostfix(en._1))
        (en._1, label, formatEnumeration(label, en._2))
      })

    val enums: Seq[String] = enumsCode.map(_._3)

    val GRList: Seq[String] = enumInfo.view.keys
      .map(k => {
        val enumName = snakeToCamel(k)
        s"    implicit def GetResult$enumName: GetResult[$enumName.Value] = { rs => $enumName.withName(rs.nextString()) }"
      })
      .toSeq

    makeSourceFileContent(pkg, enums, GRList)
  }

  private def makeSourceFileContent(pkg: String, items1: Seq[String], items2: Seq[String]): String = {
    s"""
package $pkg
import slick.jdbc.GetResult
// AUTO-GENERATED FILE, DO NOT MODIFY
// GENERATED TIME: ${new Date()}
object enums {
${items1.mkString("\n")}
  object gr {
${items2.mkString("\n")}
  }
}

""".stripMargin
  }
}

object PGCodeHelper {
  def gen(url: String, user: String, password: String, schema: String) = {
    val enumInfo = mutable.HashMap.empty[String, Seq[(String, Int)]]
    using(DriverManager.getConnection(url, user, password)) { conn =>
      using(conn.createStatement()) { sm =>
        try {
          val rs = sm.executeQuery(s"""|select t.typname, e.enumlabel, e.enumsortorder
                                       |from pg_enum e
                                       |         inner join pg_type t on t.oid = e.enumtypid
                                       |             inner join pg_namespace n on n.oid = t.typnamespace
                                       |where n.nspname = '$schema'""".stripMargin)
          while (rs.next()) {
            val name  = rs.getString("typname")
            val label = rs.getString("enumlabel")
            val order = rs.getInt("enumsortorder")
            enumInfo += (name -> enumInfo.getOrElse(name, Seq()).+:((label, order)))

          }
        } catch {
          case e: Exception => println(e)
        }
      }
    }
    enumInfo
  }

  def removeTypePostfix(s: String): String = {
    s.replaceFirst("_t$", "")
  }

  def enumNameToScalaClassName(s: String): String = {
    s.replace("$", "")
  }

  def snakeToCamel(s: String): String = {
    val tokens = s.split("_")
    tokens.map(_.capitalize).mkString
  }

  def enumNameToListName(s: String): String = {
    s"_$s"
  }

  def defineAllEnumsMap(enums: Seq[(String, String)]): String = {
    s"""  val allEnums: Map[String, Enumeration] = Map(
    ${enums.map(e => toSingleEnumListElement(e._1, e._2)).mkString(",\n    ")}
  )""".stripMargin
  }

  def toSingleEnumListElement(originalName: String, className: String): String = {
    s""""$originalName" -> $className"""
  }

  def formatEnumeration(label: String, values: Seq[String]): String = {
    s"""  object $label extends Enumeration {
    type $label = Value
    ${values.map(toSingleEnumerationDefinition).mkString("\n    ")}
  }
""".stripMargin
  }

  def toSingleEnumerationDefinition(v: String): String = {
    s"""val ${snakeToCamel(v)} = Value("$v")"""
  }
}
