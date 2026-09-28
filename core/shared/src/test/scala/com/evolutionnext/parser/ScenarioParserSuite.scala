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

import com.evolutionnext.gherkin.FeatureElement.Scenario
import com.evolutionnext.gherkin.Step
import com.evolutionnext.gherkin.StepKeyword.*
import munit.FunSuite

class ScenarioParserSuite extends FunSuite {
  test("test a scenario with a tag line") {
    val text: String =
      """@one @two
        |Scenario: A calculator should be able to divide
        |   Given a number 120
        |   And another number 2
        |   When those numbers are divided
        |   Then we should get an answer of 60""".stripMargin

    val parseResult = ScenarioParser.scenario.parseAll(text)
    parseResult match {
      case Right(scenario) => assertEquals(scenario.tags.length, 2)
      case Left(parseError) => fail(ParserDiagnose.betterMessage(parseError))
    }
  }

  test("test a scenario no tag lines") {
    val text: String =
      """Scenario: A calculator should be able to divide
        |   Given a number 120
        |   And another number 2
        |   When those numbers are divided
        |   Then we should get an answer of 60""".stripMargin

    val parseResult = ScenarioParser.scenario.parseAll(text)
    val expectedScenario = Scenario(
      Nil,
      "A calculator should be able to divide",
      List(
        Step(Given, "a number 120", None),
        Step(And, "another number 2", None),
        Step(When, "those numbers are divided", None),
        Step(Then, "we should get an answer of 60", None)
      )
    )
    parseResult match {
      case Right(scenario) => assertEquals(scenario, expectedScenario)
      case Left(parseError) => fail(ParserDiagnose.betterMessage(parseError))
    }
  }

  test("test a scenario with a tag line indented") {
    val text: String =
      """  @one @two
        |  Scenario: A calculator should be able to divide
        |     Given a number 120
        |     And another number 2
        |     When those numbers are divided
        |     Then we should get an answer of 60""".stripMargin

    val parseResult = ScenarioParser.scenario.parseAll(text)
    parseResult match {
      case Right(scenario) => assertEquals(scenario.tags.length, 2)
      case Left(parseError) => fail(ParserDiagnose.betterMessage(parseError))
    }
  }
}
