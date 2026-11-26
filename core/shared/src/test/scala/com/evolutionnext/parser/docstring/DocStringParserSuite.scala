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

package com.evolutionnext.parser.docstring

import com.evolutionnext.parser.ParserDiagnose
import munit.FunSuite

class DocStringParserSuite extends FunSuite {

  val delimiter = "\"\"\""

  test("the doc string only 3 quotation marks - no media type with a new line") {
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

  test("the closing delimiter must begin on a separate line after the Doc String content") {
    val input =
      s"""$delimiter
         |Hello$delimiter
         |""".stripMargin

    assert(DocStringParser.docString.parseAll(input).isLeft)
  }

  test(
    "the doc string only 3 quotation marks - no media type without a new line after the closing delimiter") {
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

  test("the closing delimiter does not determine the Doc String margin") {
    val input =
      s"""    $delimiter
         |    first
         |      second
         |$delimiter""".stripMargin
    val expected =
      """first
        |  second""".stripMargin
    val parser = DocStringParser.docString
    parser.parseAll(input) match {
      case Right(docString) => assertEquals(docString.content, expected)
      case Left(parserError) =>
        fail(ParserDiagnose.betterMessage(parserError))
    }
  }

  test("content before the opening delimiter margin is moved to column zero") {
    val input =
      s"""    $delimiter
         |    at margin
         |      after margin
         |  before margin
         |unindented
         |    $delimiter""".stripMargin
    val expected =
      """at margin
        |  after margin
        |before margin
        |unindented""".stripMargin

    DocStringParser.docString.parseAll(input) match {
      case Right(docString) => assertEquals(docString.content, expected)
      case Left(parserError) =>
        fail(ParserDiagnose.betterMessage(parserError))
    }
  }

  test("blank content lines are preserved when the Doc String is indented") {
    val input =
      s"""    $delimiter
         |    first
         |
         |    third
         |    $delimiter""".stripMargin
    val expected =
      """first
        |
        |third""".stripMargin

    DocStringParser.docString.parseAll(input) match {
      case Right(docString) => assertEquals(docString.content, expected)
      case Left(parserError) =>
        fail(ParserDiagnose.betterMessage(parserError))
    }
  }

  test("blank content lines are preserved when the Doc String is indented different test") {
    val input =
      s"""    $delimiter
         |    first
         |
         |    third
         |    $delimiter""".stripMargin
    val expected =
      """first
        |
        |third""".stripMargin

    DocStringParser.docString.parseAll(input) match {
      case Right(docString) => assertEquals(docString.content, expected)
      case Left(parserError) =>
        fail(ParserDiagnose.betterMessage(parserError))
    }
  }

  test("tabs in the Doc String content are preserved after dedenting") {
    val input = List(
      s"    $delimiter",
      "    first\tsecond",
      "    \tindented with a tab",
      s"    $delimiter"
    ).mkString("\n")
    val expected = "first\tsecond\n\tindented with a tab"

    DocStringParser.docString.parseAll(input) match {
      case Right(docString) => assertEquals(docString.content, expected)
      case Left(parserError) =>
        fail(ParserDiagnose.betterMessage(parserError))
    }
  }

  test("mixed spaces and tabs are dedented only when they match the opening indentation") {
    val openingIndentation = " \t"
    val input =
      s"""$openingIndentation$delimiter
         |${openingIndentation}first
         |  second
         |$openingIndentation$delimiter""".stripMargin
    val expected =
      """first
        |second""".stripMargin

    DocStringParser.docString.parseAll(input) match {
      case Right(docString) => assertEquals(docString.content, expected)
      case Left(parserError) =>
        fail(ParserDiagnose.betterMessage(parserError))
    }
  }

  test("the opening delimiter must be preceded only by leading whitespace") {
    val input =
      s"""text before $delimiter
         |content
         |$delimiter""".stripMargin

    assert(DocStringParser.docString.parseAll(input).isLeft)
  }

  /*
   Some Notes:
    - Capture the whitespace string, not merely its length, because wsp also includes tabs. You can decide deliberately how mixed tabs/spaces contribute to indentation.
    - The closing delimiter does not determine the margin.
    - Detect the opening delimiter only after leading whitespace at the beginning of its line—not merely the first """ found anywhere.
    - If FeatureParser.parse still calls .trim on every input line, that must eventually change because it removes the indentation before DocStringParser can measure it.
   */
}
