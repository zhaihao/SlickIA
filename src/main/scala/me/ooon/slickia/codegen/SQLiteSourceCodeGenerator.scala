/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.slickia.codegen

import slick.model.Model

import scala.collection.mutable.ArrayBuffer

/** SQLiteSourceCodeGenerator
  *
  * @author
  *   zhaihao
  * @version 1.0
  * @since 2021/5/10
  *   16:29
  */
//noinspection NotImplementedCode
class SQLiteSourceCodeGenerator(model: Model) extends SourceCodeGenerator(model) {
  val models = ArrayBuffer.empty[String]

  override def Table = new Table(_) {
    override def TableClass = new TableClass {
      // 增加 ??
      def nonUpdate = {
        val cls = columns.filter(!_.model.nullable)

        def tupledExpr = {
          val len = cls.length
          val tupExpr = columns.indices
            .map { i =>
              val c = columns(i)
              if (!c.model.nullable) {
                if (len == 1) "t" else s"_$i"
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
                if (c.model.nullable) s"${c.name}: Option[${c.rawType}] = None"
                else s"${c.name}: ${c.exposedType}"
              )
          )
          .mkString(", ")
        if (classEnabled) {
          val prs = (parents.take(1).map(" extends " + _) ++ parents.drop(1).map(" with " + _)).mkString("")
          (if (caseClassFinal) "final " else "") +
            s"""case class $name($args)$prs"""
        } else {
          if (columns.size > 254)
            s"type $name = $types" // constructor method would exceed JVM parameter limit
          else s"""
type $name = $types
/** Constructor for $name providing default values if available in the database schema. */
def $name($args): $name = {
  ${compoundValue(columns.map(_.name))}
}
          """.trim
        }
      }
    }

  }

  def outsideCode = s"${models.mkString("\n")}"

  override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]) = {
    s"""
package $pkg
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object $container extends $container {
  val profile = $profile
}

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait $container${parentType.map(t => s" extends $t").getOrElse("")} {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  ${indent(code)}
}
$outsideCode
      """.trim()
  }

}
