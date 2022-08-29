/*
 * Copyright (c) 2021.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 *
 */

package me.ooon.slickia

import codegen._

import config.HConfig

/** CodeGen
  *
  * @author zhaihao
  * @version 1.0
  * @since 2021/4/5 23:28
  */
trait CodeGen extends orison.App with HConfig
object PGCodeGen extends CodeGen {
  val conf           = config.getConfig("slick.code.gen").getConfig("test")
  val url            = conf.getString("url")
  val user           = conf.getString("user")
  val password       = conf.getString("password")
  val outputDir      = os.Path(conf.getString("outputDir"), os.home)
  val pkg            = conf.getString("pkg")
  val schema         = conf.getString("schema")
  val autoGenColumns = conf.getStringList("autoGenColumns")

  val m = SourceCodeGenerator.run(
    profile = CodeGenPGProfile.getClass.getName.dropRight(1),
    jdbcDriver = classOf[org.postgresql.Driver].getName,
    url = url,
    outputDir = outputDir.toString(),
    pkg = pkg,
    user = Some(user),
    password = Some(password),
    codeGeneratorClass = Some(classOf[PGSourceCodeGenerator].getName),
    ignoreInvalidDefaults = false,
    outputToMultipleFiles = false
  )

  // enum
  val code     = PGEnumCodeGenerator.gen(url, user, password, pkg, schema)
  val enumFile = pkg.split("\\.").foldLeft(outputDir) { (z, a) => z / a } / "enums.scala"
  os.write.over(enumFile, code, createFolders = true)
  // profile
  val codeProfile = PGProfileCodeGenerator.gen(url, user, password, pkg, schema)
  val profileFile = outputDir / "slick" / "jdbc" / "PGProfile.scala"
  os.write.over(profileFile, codeProfile, createFolders = true)
  // format
  val format     = PGFormatCodeGenerator.gen(url, user, password, pkg, m, schema)
  val formatFile = pkg.split("\\.").foldLeft(outputDir) { (z, a) => z / a } / "formats.scala"
  os.write.over(formatFile, format, createFolders = true)
}

object SQLiteCodeGen extends CodeGen {
  SourceCodeGenerator.run(
    profile = slick.jdbc.SQLiteProfile.getClass.getName.dropRight(1),
    jdbcDriver = classOf[org.sqlite.JDBC].getName,
    url = s"jdbc:sqlite:${os.pwd.toString}/modules/tools/src/main/resources/db.sqlite",
    outputDir = (os.pwd / "modules" / "tools" / "src" / "main" / "scala").toString,
    pkg = "me.ooon.sia.tools.slick.sqlite.model",
    user = None,
    password = None,
    codeGeneratorClass = Some(SourceCodeGenerator.getClass.getName.dropRight(1)),
    ignoreInvalidDefaults = false,
    outputToMultipleFiles = false
  )
}
