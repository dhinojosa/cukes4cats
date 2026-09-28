package com.evolutionnext.parser

import com.evolutionnext.gherkin.{Cell, Row, Table}
import munit.FunSuite

class TableParserSuite extends FunSuite {
  test("A simple data table") {
    val table: String = """|| username | email              | role   |
                           || alice    | alice@example.com  | admin  |
                           || bob      | bob@example.com    | editor |
                           || carol    | carol@example.com  | viewer |""".stripMargin
    val expectedTable = Table(
      Row(Cell("username"), Cell("email"), Cell("role")),
      Row(Cell("alice"), Cell("alice@example.com"), Cell("admin")),
      Row(Cell("bob"), Cell("bob@example.com"), Cell("editor")),
      Row(Cell("carol"), Cell("carol@example.com"), Cell("viewer"))
    )
    val result = TableParser.table.parseAll(table)
    result match {
      case Right(table) => assertEquals(table, expectedTable)
      case Left(error) => fail(ParserDiagnose.betterMessage(error))
    }
  }

  test("A simple data table with carriage return at the end") {
    val table: String =
      """|| username | email              | role   |
         || alice    | alice@example.com  | admin  |
         || bob      | bob@example.com    | editor |
         || carol    | carol@example.com  | viewer |
         |""".stripMargin
    val expectedTable = Table(
      Row(Cell("username"), Cell("email"), Cell("role")),
      Row(Cell("alice"), Cell("alice@example.com"), Cell("admin")),
      Row(Cell("bob"), Cell("bob@example.com"), Cell("editor")),
      Row(Cell("carol"), Cell("carol@example.com"), Cell("viewer"))
    )
    val result = TableParser.table.parseAll(table)
    result match {
      case Right(table) => assertEquals(table, expectedTable)
      case Left(error) => fail(ParserDiagnose.betterMessage(error))
    }
  }

  test("A simple data table with indentation") {
    val table: String =
      """|    | username | email              | role   |
         |    | alice    | alice@example.com  | admin  |
         |    | bob      | bob@example.com    | editor |
         |    | carol    | carol@example.com  | viewer |
         |""".stripMargin
    val expectedTable = Table(
      Row(Cell("username"), Cell("email"), Cell("role")),
      Row(Cell("alice"), Cell("alice@example.com"), Cell("admin")),
      Row(Cell("bob"), Cell("bob@example.com"), Cell("editor")),
      Row(Cell("carol"), Cell("carol@example.com"), Cell("viewer"))
    )
    val result = TableParser.table.parseAll(table)
    result match {
      case Right(table) => assertEquals(table, expectedTable)
      case Left(error) => fail(ParserDiagnose.betterMessage(error))
    }
  }

}
