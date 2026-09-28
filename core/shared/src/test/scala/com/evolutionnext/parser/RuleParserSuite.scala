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
        assertEquals(remainder, "\n") // The remainder should be untouched, parse is good for that
      }
      case Left(error) => fail(ParserDiagnose.betterMessage(error))
    }
  }
}
