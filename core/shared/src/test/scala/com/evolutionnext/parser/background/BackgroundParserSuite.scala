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

package com.evolutionnext.parser.background

import cats.data.NonEmptyList
import cats.parse.Parser
import cats.parse.Parser.Expectation.WithContext
import com.evolutionnext.gherkin.*
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class BackgroundParserSuite extends ScalaCheckSuite {

  test("parse a background with multiple steps") {
    val input =
      """Background:
        |  Given an empty shopping cart
        |  And a configured catalog
        |""".stripMargin

    val expected =
      Background(
        NonEmptyList.of(
          Step(StepKeyword.Given, "an empty shopping cart", None),
          Step(StepKeyword.And, "a configured catalog", None)
        )
      )

    assertEquals(BackgroundParser.background.parseAll(input), Right(expected))
  }

  test("parse a background with multiple steps and a margin") {
    val input =
      """|   Background:
         |     Given an empty shopping cart
         |     And a configured catalog
         |""".stripMargin

    val expected =
      Background(
        NonEmptyList.of(
          Step(StepKeyword.Given, "an empty shopping cart", None),
          Step(StepKeyword.And, "a configured catalog", None)
        )
      )

    assertEquals(BackgroundParser.background.parseAll(input), Right(expected))
  }

  test("parse a background with no visible steps") {
    def assertLeftHasContext(pe: Parser.Error): Unit = {
      val expectedError: Parser.Expectation = pe.expected.head
      expectedError match {
        case WithContext(contextStr, _) => assertEquals(clue(contextStr), "background-steps")
        case _ => fail("expected a with context")
      }
    }
    val input =
      """|   Background:
         |""".stripMargin

    val result: Either[Parser.Error, Background] = BackgroundParser.background.parseAll(input)
    assert(clue(result).isLeft)
    result match {
      case Left(error) => assertLeftHasContext(error)
      case Right(_) => fail("Expected Left but got Right")
    }
  }

  property("a Background without Steps never parses") {
    forAll(Gen.choose(0, 8)) { indentation =>
      val input = s"${" " * indentation}Background:\n"

      assert(
        BackgroundParser.background.parseAll(input).isLeft
      )
    }
  }
}
