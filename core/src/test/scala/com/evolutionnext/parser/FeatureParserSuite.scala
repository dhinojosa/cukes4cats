package com.evolutionnext.parser

import cats.data.EitherT
import cats.effect.*
import cats.parse.Parser
import cats.syntax.all.*
import com.evolutionnext.gherkin.*
import com.evolutionnext.gherkin.CukeError.{ParserError, RunnerError}
import com.evolutionnext.gherkin.FeatureElement.{Scenario, ScenarioOutline}
import com.evolutionnext.gherkin.StepKeyword.Given
import munit.CatsEffectSuite

import scala.io.Source

class FeatureParserSuite extends CatsEffectSuite {
  test("canary test") {
    assert(true)
  }

  test("feature header") {
    val text = "Feature: How soon is now\n"
    val result: Either[Parser.Error, Feature] = FeatureParser.featureHeader.parseAll(text)
    result.fold(pe => fail(pe.toString), feature => println(feature))
  }

  test("read in 000-basic-scenario.feature content") {
    val io: IO[String] = {
      Resource
        .make(IO(Source.fromResource("000-basic-scenario.feature")))(source =>
          IO(source.close()))
        .use(source => IO(source.mkString))
    }
    val content: String = io.unsafeRunSync()
    assert(content.nonEmpty)
    assert(content.contains("Feature: Basic arithmetic"))
  }

  test("read in 000-basic-scenario.feature and parse to Feature") {
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
    assertEquals(feature.featureElements.length, 1)

    val featureElement = feature.featureElements.head

    val scenario = featureElement match {
      case s: FeatureElement.Scenario => s
      case _ => fail("Expected a scenario")
    }

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
    assert(feature.featureElements.nonEmpty)
    val featureElement = feature.featureElements.head
    val scenario = featureElement match {
      case s: FeatureElement.Scenario => s
      case _ => fail("Expected a scenario")
    }
    assertEquals(scenario.name, "Add two numbers")
    assertEquals(scenario.steps.size, 4)
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
    assert(feature.featureElements.nonEmpty)

    val featureElement = feature.featureElements.head
    val scenario = featureElement match {
      case s: FeatureElement.Scenario => s
      case _ => fail("Expected a scenario")
    }
    assertEquals(scenario.name, "Add two numbers")
    assertEquals(scenario.steps.size, 4)
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
    assert(feature.featureElements.nonEmpty)
    assertEquals(feature.featureElements.length, 2)
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
    assert(feature.featureElements.nonEmpty)
    assertEquals(feature.featureElements.length, 2)
    val featureElement = feature.featureElements.head
    val scenario = featureElement match {
      case s: Scenario => s
      case _ => fail("Expected a scenario")
    }
    assertEquals(scenario.tags, List(Tag("Baz")))
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

  def readFromFile(s: String): EitherT[IO, CukeError, String] = {
    val io: IO[String] =
      Resource
        .make(IO(Source.fromResource(s)))(source => IO(source.close()))
        .use(source => IO(source.mkString))

    EitherT(io.attempt.map {
      case Left(t) => Left(CukeError.IOError(t.getMessage))
      case Right(s) => Right(s)
    })
  }

  def parseFeature(s: String): EitherT[IO, CukeError, Feature] =
    EitherT.fromEither[IO](FeatureParser.parse(s).leftMap { e =>
      CukeError.ParserError(ParserDiagnose.betterMessage(e), e.expected)
    })

  test("parse 002-data-tables.feature") {
    def firstGivenStep(f: FeatureElement): EitherT[IO, CukeError, Step] = {
      f match {
        case Scenario(_, _, steps) =>
          EitherT.fromOption(
            steps.find(s => s.keyword == Given),
            CukeError.RunnerError("Cannot find a given element"))
        case _ => EitherT.leftT(CukeError.RunnerError("Cannot find a scenario"))
      }
    }

    def firstFeatureElement(feature: Feature): EitherT[IO, CukeError, FeatureElement] = {
      EitherT.fromOption[IO](
        feature.featureElements.headOption,
        CukeError.RunnerError("Cannot find a feature element"))
    }

    def findTable(s: Step): EitherT[IO, CukeError, Table] =
      EitherT.fromOption(s.table, CukeError.RunnerError("Cannot find table"))

    val tableT: EitherT[IO, CukeError, Table] = for {
      content <- readFromFile("002-data-tables.feature")
      feature <- parseFeature(content)
      featureElement <- firstFeatureElement(feature)
      firstGivenStep <- firstGivenStep(featureElement)
      table <- findTable(firstGivenStep)
    } yield table

    tableT.value.map {
      case Left(cukeError) =>
        fail(cukeError.toString)
      case Right(table) =>
        assertEquals(table.row.size, 4)
    }
  }

  test("restOfLine parses scenario outline name") {
    assertEquals(
      FeatureParser.restOfLine.parseAll("Apply discount to total\n"),
      Right("Apply discount to total")
    )
  }

  test("Scenario Outline Header parses") {
    val result = FeatureParser
      .scenarioOutlineHeader
      .parseAll("   Scenario Outline: Apply discount to total\n")
    result match {
      case Right(ScenarioOutline(_, name, _, _)) =>
        assertEquals(name, "Apply discount to total")
      case Left(e) => fail(e.toString)
    }
  }

  test("parse 003-scenario-outline.feature") {
    def findFirstScenarioOutline(f: Feature): EitherT[IO, CukeError, ScenarioOutline] = {
      val opt: Option[ScenarioOutline] =
        f.featureElements.collectFirst { case so: ScenarioOutline => so }
      EitherT.fromOption[IO](opt, RunnerError("Cannot find Scenario Outline"))
    }

    def assertExpectedScenarioOutline(featureElement: FeatureElement): Unit = {
      featureElement match {
        case so: ScenarioOutline => {
          assertEquals(so.name, "Apply discount to total")
          assertEquals(so.steps.size, 3)
        }
        case _ => fail("Expected a Scenario Outline")
      }
    }

    def assertExpectedFeature(feature: Feature): Unit = {
      assertEquals(feature.name, "Discount calculation")
      assertEquals(feature.featureElements.size, 1)
      assertExpectedScenarioOutline(feature.featureElements.head)
    }

    val featureResult: EitherT[IO, CukeError, Feature] = for {
      content <- readFromFile("003-scenario-outline.feature")
      feature <- parseFeature(content)
    } yield feature

    featureResult.value.map {
      case Right(feature) => assertExpectedFeature(feature)
      case Left(cukeError) => fail(cukeError.toString)
    }
  }
}
