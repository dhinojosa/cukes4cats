package com.evolutionnext.parser

import cats.parse.Parser as P
import com.evolutionnext.gherkin.StepArgument.DocString
import munit.FunSuite

class DocStringParserSuite extends FunSuite {
  test("the doc string only 3 quotation marks - no media type with a new line") {
    val delimiter = "\"\"\""
    val input =
      s"""$delimiter
         |Hello
         |$delimiter
         |""".stripMargin
    val parser = DocStringParser.docString
    parser.parseAll(input) match {
      case Right(docString) => assertEquals(docString.content, "Hello")
      case Left(parserError) => {
        fail(ParserDiagnose.betterMessage(parserError))
      }
    }
  }

  test("the doc string only 3 quotation marks - no media type without a new line") {
    val delimiter = "\"\"\""
    val input =
      s"""$delimiter
         |Hello$delimiter
         |""".stripMargin
    val parser = DocStringParser.docString
    parser.parseAll(input) match {
      case Right(docString) => assertEquals(docString.content, "Hello")
      case Left(parserError) => {
        fail(ParserDiagnose.betterMessage(parserError))
      }
    }
  }

  test(
    "the doc string only 3 quotation marks - no media type without a new line after the closing delimiter") {
    val delimiter = "\"\"\""
    val input =
      s"""$delimiter
         |Hello
         |$delimiter""".stripMargin
    val parser = DocStringParser.docString
    parser.parseAll(input) match {
      case Right(docString) => assertEquals(docString.content, "Hello")
      case Left(parserError) => {
        fail(ParserDiagnose.betterMessage(parserError))
      }
    }
  }

  test(
    """Each line of the Doc String will be dedented according to the opening triple quote.
      |  Indentation beyond the column of the opening triple quote will therefore be preserved.""".stripMargin) {
    val delimiter = "\"\"\""
    val input: String =
      s"""    $delimiter
         |    first
         |      second
         |    third
         |    $delimiter""".stripMargin
    val expected =
      s"""first
         |  second
         |third""".stripMargin
    val parser = DocStringParser.docString
    parser.parseAll(input) match {
      case Right(docString) => assertEquals(docString.content, expected)
      case Left(parserError) =>
        fail(ParserDiagnose.betterMessage(parserError))
    }
  }

  /*
   Some Notes:
    - Capture the whitespace string, not merely its length, because wsp also includes tabs. You can decide deliberately how mixed tabs/spaces contribute to indentation.
    - The closing delimiter does not determine the margin.
    - Detect the opening delimiter only after leading whitespace at the beginning of its line—not merely the first """ found anywhere.
    - If FeatureParser.parse still calls .trim on every input line, that must eventually change because it removes the indentation before DocStringParser can measure it.
   */
}
