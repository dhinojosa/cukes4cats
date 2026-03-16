package com.evolutionnext.parser

import cats.effect.*
import cats.effect.unsafe.implicits.global
import com.evolutionnext.gherkin.Feature

import scala.io.Source

class FeatureParserSuite extends munit.FunSuite {
  test("canary test") {
    assert(true)
  }
  test("read in 000-basic-scenario.feature content") {
    val io: IO[String] = {
      Resource
        .make(IO(Source.fromResource("000-basic-scenario.feature")))(source =>
          IO(source.close()))
        .use(source => IO(source.mkString))
    }

    val content = io.unsafeRunSync()
    assert(content.nonEmpty)
    assert(content.contains("Feature: Basic arithmetic"))
  }
  test("read in 000-basic-scenario.feature") {
    val io: IO[String] = {
      Resource
        .make(IO(Source.fromResource("000-basic-scenario.feature")))(source =>
          IO(source.close()))
        .use(source => IO(source.mkString))
    }

    val content = io.unsafeRunSync()
    val feature: Feature = FeatureParser.parse(content)

    assertEquals(feature.name, "Basic arithmetic")
    assertEquals(feature.tags, Nil)
    assertEquals(feature.description, Nil)
    assertEquals(feature.scenarios.length, 1)

    val scenario = feature.scenarios.head
    assertEquals(scenario.name, "Add two numbers")
    assertEquals(scenario.tags, Nil)

    assertEquals(
      scenario.steps,
      List(
        com
          .evolutionnext
          .gherkin
          .Step(com.evolutionnext.gherkin.StepKeyword.Given, "the number 2"),
        com
          .evolutionnext
          .gherkin
          .Step(com.evolutionnext.gherkin.StepKeyword.And, "the number 3"),
        com
          .evolutionnext
          .gherkin
          .Step(com.evolutionnext.gherkin.StepKeyword.When, "I add the numbers"),
        com
          .evolutionnext
          .gherkin
          .Step(com.evolutionnext.gherkin.StepKeyword.Then, "the result should be 5")
      )
    )
  }
}
