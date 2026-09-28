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
