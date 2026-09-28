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

import com.evolutionnext.gherkin.{Cell, Example, Row, Table}
import munit.FunSuite

class ExamplesParserSuite extends FunSuite {
  test("test an example with the table flushed to the left") {
    val text = """Examples: Domestic
                 || weight | region   | cost |
                 || 1      | US       | 5.00 |
                 || 5      | US       | 8.50 |""".stripMargin
    val result = ExamplesParser.examplesChoiceWithTable.parseAll(text)
    val expected = Example(
      Option("Domestic"),
      Table(
        Row(Cell("weight"), Cell("region"), Cell("cost")),
        Row(Cell("1"), Cell("US"), Cell("5.00")),
        Row(Cell("5"), Cell("US"), Cell("8.50"))
      )
    )
    result match {
      case Right(example) => assertEquals(example, expected)
      case Left(error) => fail(ParserDiagnose.betterMessage(error))
    }
  }

  test("test an example with the table flushed to the left with an extra margin") {
    val text =
      """Examples: Domestic
        |  | weight | region   | cost |
        |  | 1      | US       | 5.00 |
        |  | 5      | US       | 8.50 |""".stripMargin
    val result = ExamplesParser.examplesChoiceWithTable.parseAll(text)
    val expected = Example(
      Option("Domestic"),
      Table(
        Row(Cell("weight"), Cell("region"), Cell("cost")),
        Row(Cell("1"), Cell("US"), Cell("5.00")),
        Row(Cell("5"), Cell("US"), Cell("8.50"))
      )
    )
    result match {
      case Right(example) => assertEquals(example, expected)
      case Left(error) => fail(ParserDiagnose.betterMessage(error))
    }
  }
}
