/*
 * Copyright (c) 2026 Evolutionnext
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.evolutionnext.parser

import com.evolutionnext.gherkin.{Cell, Row, Table}
import com.evolutionnext.parser.ParserDiagnose.betterMessage
import munit.FunSuite

class TableParserSuite extends FunSuite {
  test("A simple data table") {
    val text: String = """|| username | email              | role   |
                          || alice    | alice@example.com  | admin  |
                          || bob      | bob@example.com    | editor |
                          || carol    | carol@example.com  | viewer |""".stripMargin
    val expectedTable = Table(
      Row(Cell("username"), Cell("email"), Cell("role")),
      Row(Cell("alice"), Cell("alice@example.com"), Cell("admin")),
      Row(Cell("bob"), Cell("bob@example.com"), Cell("editor")),
      Row(Cell("carol"), Cell("carol@example.com"), Cell("viewer"))
    )
    val result = TableParser.table.parseAll(text)
    result match {
      case Right(table) => assertEquals(table, expectedTable)
      case Left(error) => fail(betterMessage(error))
    }
  }

  test("A simple data table with carriage return at the end") {
    val text: String =
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
    val result = TableParser.table.parseAll(text)
    result match {
      case Right(table) => assertEquals(table, expectedTable)
      case Left(error) => fail(betterMessage(error))
    }
  }

  test("A simple data table with indentation") {
    val text: String =
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
    val result = TableParser.table.parseAll(text)
    result match {
      case Right(table) => assertEquals(table, expectedTable)
      case Left(error) => fail(betterMessage(error))
    }
  }
}
