package com.evolutionnext.parser

import cats.effect.*
import cats.effect.unsafe.implicits.global
import cats.parse.Parser
import com.evolutionnext.gherkin.{Feature, Tag}
import munit.Clue.generate

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
        val eitherFeature: Either[Parser.Error, Feature] = FeatureParser.parse(content)
        val feature = eitherFeature match {
            case Right(feature) => feature
            case Left(error) => fail(s"could not parse feature: $error")
        }

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

    test("basic parse with annotation") {
        val string =
            """@Foo
              |Feature: Basic arithmetic
              |
              |    Scenario: Add two numbers
              |        Given the number 2
              |        And the number 3
              |        When I add the numbers
              |        Then the result should be 5
              |""".stripMargin

        val eitherFeature: Either[Parser.Error, Feature] = FeatureParser.parse(string)
        val feature = eitherFeature match {
            case Right(feature) => feature
            case Left(error) => fail(s"could not parse feature: $error")
        }
        assertEquals(feature.tags, List(Tag("Foo")))
        assertEquals(feature.name, "Basic arithmetic")
        assert(feature.scenarios.nonEmpty)
        assertEquals(feature.scenarios.head.name, "Add two numbers")
        assertEquals(feature.scenarios.head.steps.size, 4)
    }

    test("basic parse with two annotations") {
        val string =
            """@Foo @Bar
              |Feature: Basic arithmetic
              |
              |    Scenario: Add two numbers
              |        Given the number 2
              |        And the number 3
              |        When I add the numbers
              |        Then the result should be 5
              |""".stripMargin

        val eitherFeature: Either[Parser.Error, Feature] = FeatureParser.parse(string)
        val feature = eitherFeature match {
            case Right(feature) => feature
            case Left(error) => fail(s"could not parse feature: $error")
        }

        assertEquals(feature.tags, List(Tag("Foo"), Tag("Bar")))
        assertEquals(feature.name, "Basic arithmetic")
        assert(feature.scenarios.nonEmpty)
        assertEquals(feature.scenarios.head.name, "Add two numbers")
        assertEquals(feature.scenarios.head.steps.size, 4)
    }

    test("basic parse with two annotations and two scenarios") {
        val string =
            """@Foo @Bar
              |Feature: Basic arithmetic
              |
              |    Scenario: Add two numbers
              |        Given the number 2
              |        And the number 3
              |        When I add the numbers
              |        Then the result should be 5
              |
              |    Scenario: Add two different numbers
              |        Given the number 5
              |        And the number 7
              |        When I add the numbers
              |        Then the result should be 12
              |""".stripMargin

        val eitherFeature: Either[Parser.Error, Feature] = FeatureParser.parse(string)
        val feature = eitherFeature match {
            case Right(feature) => feature
            case Left(error) => fail(s"could not parse feature: $error")
        }
        assertEquals(feature.tags, List(Tag("Foo"), Tag("Bar")))
        assertEquals(feature.name, "Basic arithmetic")
        assert(feature.scenarios.nonEmpty)
        assertEquals(feature.scenarios.length, 2)
    }

    test("basic parse with two annotations and two scenarios, tags on the first") {
        val string =
            """@Foo @Bar
              |Feature: Basic arithmetic
              |
              |    @Baz
              |    Scenario: Add two numbers
              |        Given the number 2
              |        And the number 3
              |        When I add the numbers
              |        Then the result should be 5
              |
              |    Scenario: Add two different numbers
              |        Given the number 5
              |        And the number 7
              |        When I add the numbers
              |        Then the result should be 12
              |""".stripMargin

        val eitherFeature: Either[Parser.Error, Feature] = FeatureParser.parse(string)
        val feature = eitherFeature match {
            case Right(feature) => feature
            case Left(error) => fail(s"could not parse feature: $error")
        }
        assertEquals(feature.tags, List(Tag("Foo"), Tag("Bar")))
        assertEquals(feature.name, "Basic arithmetic")
        assert(feature.scenarios.nonEmpty)
        assertEquals(feature.scenarios.length, 2)
        assertEquals(feature.scenarios.head.tags, List(Tag("Baz")))
    }

    test("parse 001-annotation-scenario.feature with a feature tag") {
        val io: IO[String] =
            Resource
                .make(IO(Source.fromResource("001-annotation-scenario.feature")))(source =>
                    IO(source.close()))
                .use(source => IO(source.mkString))

        val eitherFeature: Either[Parser.Error, Feature] =
            io.map(FeatureParser.parse).unsafeRunSync()
        val feature: Feature = eitherFeature match {
            case Right(feature) => feature
            case Left(error) => fail(s"could not parse feature: $error")
        }
        assertEquals(feature.tags, List(Tag("fast"), Tag("unit")))
    }

    test("parse 002-data-tables.feature") {
        val io: IO[String] =
            Resource
                .make(IO(Source.fromResource("002-data-tables.feature")))(source => IO(source.close()))
                .use(source => IO(source.mkString))
        val eitherFeature: Either[Parser.Error, Feature] =
            io.map(FeatureParser.parse).unsafeRunSync()
        val feature: Feature = eitherFeature match {
            case Right(feature) => feature
            case Left(error) => fail(s"could not parse feature: $error")
        }
    }
}
