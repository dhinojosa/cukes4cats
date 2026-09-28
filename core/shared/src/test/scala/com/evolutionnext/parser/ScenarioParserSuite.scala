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
