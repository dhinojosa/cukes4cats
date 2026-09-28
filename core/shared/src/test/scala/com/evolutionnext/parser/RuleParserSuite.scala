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

import com.evolutionnext.gherkin.Rule
import munit.FunSuite

class RuleParserSuite extends FunSuite {
  test("A rule with one scenario") {
    val text =
      """Rule: Make tortillas
        |
        |   Scenario: Mixing the ingredients
        |     Given flour
        |     And water
        |     When mixed in a bowl
        |     Then it is ready to be made""".stripMargin
    val result = RuleParser.rule.parseAll(text)
    result match {
      case Right(Rule(text, featureElements)) => assertEquals(text, "Make tortillas")
      case Left(error) => fail(ParserDiagnose.betterMessage(error))
    }
  }

  test("A rule with one scenario extra lf") {
    val text =
      """Rule: Make tortillas
        |
        |   Scenario: Mixing the ingredients
        |     Given flour
        |     And water
        |     When mixed in a bowl
        |     Then it is ready to be made
        |""".stripMargin
    val result = RuleParser.rule.parseAll(text)
    result match {
      case Right(Rule(text, featureElements)) => assertEquals(text, "Make tortillas")
      case Left(error) => fail(ParserDiagnose.betterMessage(error))
    }
  }

  test("A rule with one scenario two extra lf") {
    val text =
      """Rule: Make tortillas
        |
        |   Scenario: Mixing the ingredients
        |     Given flour
        |     And water
        |     When mixed in a bowl
        |     Then it is ready to be made
        |
        |""".stripMargin
    val result = RuleParser.rule.parse(text)
    result match {
      case Right((remainder, Rule(text, featureElements))) => {
        assertEquals(text, "Make tortillas")
        assertEquals(
          remainder,
          "\n"
        ) // The remainder should be untouched, parse is good for that
      }
      case Left(error) => fail(ParserDiagnose.betterMessage(error))
    }
  }
}
