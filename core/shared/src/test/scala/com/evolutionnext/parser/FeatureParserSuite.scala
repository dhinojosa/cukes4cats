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

import cats.data.{EitherT, NonEmptyList}
import cats.effect.*
import cats.parse.Parser
import cats.syntax.all.*
import com.evolutionnext.gherkin.*
import com.evolutionnext.gherkin.FeatureElement.{Scenario, ScenarioOutline}
import com.evolutionnext.gherkin.StepArgument.DataTable
import com.evolutionnext.gherkin.StepKeyword.{And, Given}
import munit.CatsEffectSuite

import scala.io.Source

class FeatureParserSuite extends CatsEffectSuite {
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
        com.evolutionnext.gherkin.Step(Given, "the number 2", Option.empty),
        com
          .evolutionnext
          .gherkin
          .Step(com.evolutionnext.gherkin.StepKeyword.And, "the number 3", Option.empty),
        com
          .evolutionnext
          .gherkin
          .Step(com.evolutionnext.gherkin.StepKeyword.When, "I add the numbers", Option.empty),
        com
          .evolutionnext
          .gherkin
          .Step(
            com.evolutionnext.gherkin.StepKeyword.Then,
            "the result should be 5",
            Option.empty)
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

  test("parse 001-annotation-scenario.feature with a feature tag") {
    def assertTags(feature: Feature): Unit =
      assertEquals(feature.tags, List(Tag("fast"), Tag("unit")))

    val featureResult: EitherT[IO, CukeError, Feature] = for {
      content <- readFromFile("001-annotation-scenario.feature")
      feature <- parseFeature(content)
    } yield feature

    featureResult.value.map {
      case Right(feature) => assertTags(feature)
      case Left(cukeError) => fail(cukeError.toString)
    }
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
    def assertFeatureElement(featureElement: FeatureElement): Unit = {
      featureElement match {
        case Scenario(tags, name, steps) =>
          assert(tags.isEmpty)
          assertEquals(name, "Register several users")
          assertEquals(steps.length, 3)
        case _ => fail("Expected a scenario")
      }
    }

    def assertExpectedFeature(feature: Feature): Unit = {
      assertEquals(feature.name, "Bulk user registration")
      val elements = feature.featureElements
      assertEquals(elements.length, 1)
      elements.headOption match {
        case Some(featureElement) => assertFeatureElement(featureElement)
        case None => fail("Expected a feature element")
      }
    }

    val featureResult: EitherT[IO, CukeError, Feature] = for {
      content <- readFromFile("002-data-tables.feature")
      feature <- parseFeature(content)
    } yield feature

    featureResult.value.map {
      case Right(feature) => assertExpectedFeature(feature)
      case Left(cukeError) => fail(cukeError.toString)
    }
  }

  test("parse 003-scenario-outline.feature") {
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

  test("parse 004-multiple-example-blocks.feature") {

    def assertExpectedFeature(feature: Feature): Unit = {
      assert(feature.name == "Shipping cost")
      assertFeatureElements(feature.featureElements)
    }

    def assertFeatureElements(featureElements: List[FeatureElement]): Unit = {
      assert(featureElements.length == 1)
      featureElements.headOption match {
        case Some(fe) => assertScenarioOutline(fe)
        case None => fail("No feature element found")
      }
    }

    def assertSteps(steps: NonEmptyList[Step]): Unit = {
      val expected: NonEmptyList[Step] = NonEmptyList(
        Step(Given, "a package weight of <weight>", Option.empty),
        List(
          Step(StepKeyword.And, "a destination region of \"<region>\"", Option.empty),
          Step(StepKeyword.When, "I calculate shipping", Option.empty),
          Step(StepKeyword.Then, "the shipping cost should be <cost>", Option.empty)
        )
      )
      assertEquals(steps, expected)
    }

    def assertExamples(examples: NonEmptyList[Example]): Unit = {
      def row(values: String*): Row = Row(values.map(Cell.apply): _*)

      val expected: NonEmptyList[Example] = NonEmptyList.of(
        Example(
          Some("Domestic"),
          Table(
            row("weight", "region", "cost"),
            row("1", "US", "5.00"),
            row("5", "US", "8.50")
          )
        ),
        Example(
          Some("International"),
          Table(
            row("weight", "region", "cost"),
            row("1", "EU", "12.00"),
            row("5", "APAC", "20.00")
          )
        )
      )

      assertEquals(examples, expected)
    }

    def assertScenarioOutline(featureElement: FeatureElement): Unit = {
      featureElement match {
        case ScenarioOutline(tags, name, steps, examples) => {
          assert(tags.isEmpty)
          assert(name == "Shipping rates vary by region")
          assertSteps(steps)
          assertExamples(examples)
        }
        case _ => fail("Not a scenario outline")
      }
    }

    val featureResult: EitherT[IO, CukeError, Feature] = for {
      content <- readFromFile("004-multiple-example-blocks.feature")
      feature <- parseFeature(content)
    } yield feature

    featureResult.value.map {
      case Right(feature) => assertExpectedFeature(feature)
      case Left(cukeError) => fail(cukeError.toString)
    }
  }

  test("parse 005-background-scenario.feature") {

    def assertBackground(background: Background): Unit = {
      def row(values: String*): Row = Row(values.map(Cell.apply): _*)

      val expectedTable = Table(
        row("sku", "name", "price"),
        row("A100", "Notebook", "10.00"),
        row("B200", "Pencil", "2.50")
      )

      val expectedSteps = NonEmptyList(
        Step(Given, "an empty shopping cart", Option.empty),
        List(Step(And, "a catalog with the following items:", Some(DataTable(expectedTable))))
      )

      assertEquals(background.steps, expectedSteps)
    }

    def assertExpectedBackground(feature: Feature): Unit = {
      feature.background match {
        case Some(background) => assertBackground(background)
        case None => fail("Expected a Background")
      }
    }

    val featureResult: EitherT[IO, CukeError, Feature] = for {
      content <- readFromFile("005-background-scenario.feature")
      feature <- parseFeature(content)
    } yield feature

    featureResult.value.map {
      case Right(feature) => assertExpectedBackground(feature)
      case Left(cukeError) => fail(cukeError.toString)
    }
  }

  test("parse 006-doc-strings.feature") {
    def assertExpectedGivenStepWithDocString(steps: List[Step]): Unit = {
      steps.headOption match {
        case Some(
              Step(StepKeyword.Given, text, Some(StepArgument.DocString(content, None)))) => {
          val expectedText = """{
                               |  "name": "Ada",
                               |  "email": "ada@example.com",
                               |  "active": true
                               |}""".stripMargin
          assertEquals(expectedText, content)
        }
        case _ => fail("Expected a head with Given and a Doc String")
      }
    }

    def assertExpectedScenario(featureElement: FeatureElement): Unit = {
      featureElement match {
        case Scenario(tags, name, steps) => assertExpectedGivenStepWithDocString(steps)
        case _ => fail("Expected a scenario")
      }
    }

    def assertExpectedFeatureElement(feature: Feature): Unit = {
      feature.featureElements.headOption match {
        case Some(featureElement) => assertExpectedScenario(featureElement)
        case None => fail("expected a feature element")
      }
    }

    val featureResult: EitherT[IO, CukeError, Feature] = for {
      content <- readFromFile("006-doc-strings.feature")
      feature <- parseFeature(content)
    } yield feature

    featureResult.value.map {
      case Right(feature) => assertExpectedFeatureElement(feature)
      case Left(cukeError) => fail(cukeError.toString)
    }
  }

  test("parse 007-rule-blocks.feature") {
    def assertFeatureHasRules(feature: Feature): Unit = {
      val expectedRules = List(
        Rule(
          "Password must be at least 8 characters",
          NonEmptyList.one(
            Scenario(
              tags = Nil,
              name = "Reject short password",
              steps = List(
                Step(Given, "a password of \"short\"", Option.empty),
                Step(StepKeyword.When, "I validate the password", Option.empty),
                Step(StepKeyword.Then, "the password should be rejected", Option.empty)
              )
            )
          )
        ),
        Rule(
          "Password must contain a number",
          NonEmptyList.one(
            Scenario(
              tags = Nil,
              name = "Reject password without number",
              steps = List(
                Step(Given, "a password of \"longpassword\"", Option.empty),
                Step(StepKeyword.When, "I validate the password", Option.empty),
                Step(StepKeyword.Then, "the password should be rejected", Option.empty)
              )
            )
          )
        )
      )

      assertEquals(feature.name, "Password policy")
      assertEquals(feature.background, None)
      assertEquals(feature.featureElements, Nil)
      assertEquals(feature.rules, expectedRules)
    }

    val featureResult: EitherT[IO, CukeError, Feature] = for {
      content <- readFromFile("007-rule-blocks.feature")
      feature <- parseFeature(content)
    } yield feature

    featureResult.value.map {
      case Right(feature) => assertFeatureHasRules(feature)
      case Left(cukeError) => fail(cukeError.toString)
    }
  }
}
