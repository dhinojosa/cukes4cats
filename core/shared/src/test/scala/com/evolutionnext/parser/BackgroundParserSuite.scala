package com.evolutionnext.parser

import cats.data.NonEmptyList
import com.evolutionnext.gherkin.*
import munit.FunSuite

class BackgroundParserSuite extends FunSuite {

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
}
