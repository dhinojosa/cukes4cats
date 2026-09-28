package com.evolutionnext.parser
import cats.parse.Parser as P
import cats.parse.Rfc5234.wsp
import com.evolutionnext.gherkin.FeatureElement.Scenario
import com.evolutionnext.parser.CatsParseSupport.*
import com.evolutionnext.parser.StepParser.step
import com.evolutionnext.parser.TagParser.tagLine

object ScenarioParser {
  private[parser] val scenarioHeader: P[String] =
    wsp.rep0.with1 *> (P.string("Scenario:") *> spaces0 *> text <* newline.?)
      .map(_.trim)
      .withContext("scenarioHeader")

  private val taggedScenario: P[Scenario] =
    (tagLine.backtrack.rep ~ scenarioHeader ~ step.rep).map {
      case ((tagLines, name), steps) =>
        Scenario(
          tags = tagLines.toList.flatten,
          name = name,
          steps = steps.toList
        )
    }

  private val untaggedScenario: P[Scenario] =
    (scenarioHeader ~ step.rep).map {
      case (name, steps) =>
        Scenario(
          tags = Nil,
          name = name,
          steps = steps.toList
        )
    }

  private[parser] val scenario: P[Scenario] =
    wsp.rep0.with1 *> taggedScenario.backtrack.orElse(untaggedScenario).withContext("scenario")
}
