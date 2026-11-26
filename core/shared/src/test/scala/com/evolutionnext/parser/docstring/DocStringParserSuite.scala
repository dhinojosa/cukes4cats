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

import com.evolutionnext.gherkin.StepArgument.DocString
import com.evolutionnext.parser.ParserDiagnose
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll

class DocStringParserSuite extends ScalaCheckSuite {

  val delimiter = "\"\"\""

  private def assertDocStringError(input: String, expected: DocStringError): Unit =
    DocStringParser.docString.parseAll(input) match {
      case Left(DocStringError(actual)) => assertEquals(actual, expected)
      case Left(parserError) =>
        fail(s"Expected $expected, but the error was not owned by DocStringError: $parserError")
      case Right(docString) =>
        fail(s"Expected $expected, but successfully parsed $docString")
    }

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

    assertDocStringError(input, DocStringError.Unterminated)
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

  test("spaces and tabs each contribute one character to the dedentation margin") {
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

    assertDocStringError(input, DocStringError.ExpectedOpeningDelimiter)
  }

  test("missing closing delimiter") {
    val input =
      s"""$delimiter
         |content""".stripMargin

    assertDocStringError(input, DocStringError.Unterminated)
  }

  test("empty doc string") {
    val input =
      s"""$delimiter
         |$delimiter""".stripMargin

    assertEquals(DocStringParser.docString.parseAll(input), Right(DocString("", None)))
  }

  test("an inline quote is acceptable") {
    val input =
      s"""${delimiter}
         |Hello $delimiter World
         |$delimiter""".stripMargin

    assertEquals(
      DocStringParser.docString.parseAll(input),
      Right(DocString("Hello \"\"\" World", None)))
  }

  test("media type should be captured") {
    val input =
      s"""${delimiter}application/json
         |{"answer": 42}
         |$delimiter""".stripMargin
    assertEquals(
      DocStringParser.docString.parseAll(input),
      Right(DocString("{\"answer\": 42}", Option("application/json"))))
  }

  test("an empty Doc String has empty content") {
    val input = List(delimiter, delimiter).mkString("\n")
    assertEquals(
      DocStringParser.docString.parseAll(input).map(_.content),
      Right("")
    )
  }

  test("closing delimiter has non-whitespace afterward") {
    val input =
      s"""$delimiter
         |content
         |$delimiter garbage""".stripMargin

    assertDocStringError(input, DocStringError.Unterminated)
  }

  property("each content line is dedented by at most the opening margin") {
    val text = Gen.nonEmptyListOf(Gen.alphaNumChar).map(_.mkString)

    forAll(Gen.choose(0, 8), Gen.choose(0, 12), text) { (margin, indentation, content) =>
      val line = (" " * indentation) + content
      val input = List(
        (" " * margin) + delimiter,
        line,
        (" " * margin) + delimiter
      ).mkString("\n")

      val expected =
        line.drop(math.min(margin, indentation))

      assertEquals(
        DocStringParser.docString.parseAll(input).map(_.content),
        Right(expected)
      )
    }
  }

  property("shifting the entire Doc String right does not change its content") {
    val text = Gen.nonEmptyListOf(Gen.alphaNumChar).map(_.mkString)

    forAll(Gen.choose(0, 8), text) { (shift, content) =>
      def render(margin: Int): String =
        List(
          (" " * margin) + delimiter,
          (" " * margin) + content,
          (" " * margin) + delimiter
        ).mkString("\n")

      val original = DocStringParser.docString.parseAll(render(0))
      val shifted = DocStringParser.docString.parseAll(render(shift))

      assertEquals(shifted, original)
    }
  }

  property("closing delimiter indentation does not affect content") {
    val text = Gen.nonEmptyListOf(Gen.alphaNumChar).map(_.mkString)

    forAll(Gen.choose(0, 8), Gen.choose(0, 8), text) {
      (openingMargin, closingMargin, content) =>
        val input = List(
          (" " * openingMargin) + delimiter,
          (" " * openingMargin) + content,
          (" " * closingMargin) + delimiter
        ).mkString("\n")

        assertEquals(
          DocStringParser.docString.parseAll(input).map(_.content),
          Right(content)
        )
    }
  }

  property("LF and CRLF Doc Strings produce the same content") {
    val text = Gen.nonEmptyListOf(Gen.alphaNumChar).map(_.mkString)

    forAll(text, text) { (first, second) =>
      def input(eol: String): String =
        List(delimiter, first, second, delimiter).mkString(eol)

      assertEquals(
        DocStringParser.docString.parseAll(input("\r\n")),
        DocStringParser.docString.parseAll(input("\n"))
      )
    }
  }
}
