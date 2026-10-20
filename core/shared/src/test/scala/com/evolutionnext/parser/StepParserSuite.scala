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

import cats.parse.Parser
import com.evolutionnext.gherkin.*
import com.evolutionnext.gherkin.StepArgument.DocString
import com.evolutionnext.gherkin.StepKeyword.Given
import munit.FunSuite

class StepParserSuite extends FunSuite {
  test("Step Parser for Given") {
    val result: Either[Parser.Error, Step] =
      StepParser.step.parseAll("Given a clown makes bread")
    def assertGiven(step: Step): Unit = {
      assertEquals(step.text, "a clown makes bread")
    }
    result match {
      case Right(step @ Step(StepKeyword.Given, text, maybeArgument)) =>
        assertGiven(step)
      case Right(_) => fail("Expected a Given")
      case Left(error) => fail(ParserDiagnose.betterMessage(error))
    }
  }
  test("Step Parser for When") {
    val result: Either[Parser.Error, Step] =
      StepParser.step.parseAll("When his horn blows")

    def assertGiven(step: Step): Unit = {
      assertEquals(step.text, "his horn blows")
    }

    result match {
      case Right(step @ Step(StepKeyword.When, text, maybeArgument)) =>
        assertGiven(step)
      case Right(_) => fail("Expected a When")
      case Left(error) => fail(ParserDiagnose.betterMessage(error))
    }
  }
  test("Step Parser for Then") {
    val result: Either[Parser.Error, Step] =
      StepParser.step.parseAll("Then he has to go")

    def assertGiven(step: Step): Unit = {
      assertEquals(step.text, "he has to go")
    }

    result match {
      case Right(step @ Step(StepKeyword.Then, text, maybeArgument)) =>
        assertGiven(step)
      case Right(_) => fail("Expected a Then")
      case Left(error) => fail(ParserDiagnose.betterMessage(error))
    }
  }
  test("Step Parser for And") {
    val result: Either[Parser.Error, Step] =
      StepParser.step.parseAll("And his watch is broken")

    def assertGiven(step: Step): Unit = {
      assertEquals(step.text, "his watch is broken")
    }

    result match {
      case Right(step @ Step(StepKeyword.And, text, maybeArgument)) =>
        assertGiven(step)
      case Right(_) => fail("Expected a And")
      case Left(error) => fail(ParserDiagnose.betterMessage(error))
    }
  }
  test("Step Parser for But") {
    val result: Either[Parser.Error, Step] =
      StepParser.step.parseAll("But his watch is broken")

    def assertGiven(step: Step): Unit = {
      assertEquals(step.text, "his watch is broken")
    }

    result match {
      case Right(step @ Step(StepKeyword.But, text, maybeArgument)) =>
        assertGiven(step)
      case Right(_) => fail("Expected a But")
      case Left(error) => fail(ParserDiagnose.betterMessage(error))
    }
  }

  test("Step with Argument") {
    val tripleQuote: String = "\"\"\""
    val text: String =
      s"""Given there is a cat in my sock
         |  $tripleQuote
         |  Some kind of text is here
         |  $tripleQuote
         |""".stripMargin

    def assertDocString(maybeStepArgument: Option[StepArgument]): Unit = {
      maybeStepArgument match {
          case Some(DocString(content, maybeMediaType)) => {
              assertEquals(content, "Some kind of text is here")
              assertEquals(maybeMediaType, Option.empty[String])
          }
          case _ => fail("unexpected content")
      }
    }

    def assertGivenWithArg(step: Step): Unit = {
      assertEquals(step.text, "there is a cat in my sock")
      assertDocString(step.stepArgument)
    }

    val result: Either[Parser.Error, Step] = StepParser.step.parseAll(text)
    result match {
        case Right(step @ Step(StepKeyword.Given, text, maybeArgument)) => assertGivenWithArg(step)
        case Right(_) => fail("Expected an argument")
        case Left(error) => fail(ParserDiagnose.betterMessage(error))
    }
  }
}
