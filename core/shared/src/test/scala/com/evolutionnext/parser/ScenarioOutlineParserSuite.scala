package com.evolutionnext.parser

import com.evolutionnext.gherkin.FeatureElement.ScenarioOutline
import munit.FunSuite

class ScenarioOutlineParserSuite extends FunSuite {
  test("test a scenario outline") {
    val text: String =
      """Scenario Outline: Apply discount to total
        |    Given a subtotal of <subtotal>
        |    When I apply a discount of <discountPercent> percent
        |    Then the final total should be <total>
        |
        |    Examples:
        |        | subtotal | discountPercent | total |
        |        | 100.00   | 10              | 90.00 |
        |        | 80.00    | 25              | 60.00 |
        |        | 50.00    | 0               | 50.00 |""".stripMargin

    val parseResult = ScenarioOutlineParser.scenarioOutline.parseAll(text)
    parseResult match {
      case Right(scenarioOutline) =>
        assertEquals(scenarioOutline.name, "Apply discount to total")
      case Left(parseError) => fail(ParserDiagnose.betterMessage(parseError))
    }
  }
}
