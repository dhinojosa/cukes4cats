package com.evolutionnext.parser
import cats.data.NonEmptyList
import cats.parse.Parser as P
import cats.parse.Rfc5234.*
import com.evolutionnext.gherkin.{Example, FeatureElement, Step}
import com.evolutionnext.gherkin.FeatureElement.ScenarioOutline
import com.evolutionnext.parser.CatsParseSupport.{endOfLine, text}
import com.evolutionnext.parser.ExamplesParser.examplesChoiceWithTable
import com.evolutionnext.parser.StepParser.step

object ScenarioOutlineParser {
  private val scenarioOutlineHeader: P[String] =
    (wsp.rep0.void.with1 *> (P
      .string("Scenario Outline:") *> text <* wsp.rep0.void *> (cr | lf).void)).map(_.trim)

  private val stepRepWithLineFeed: P[NonEmptyList[Step]] = (cr | lf).rep0.void.with1 *> step.rep

  private val examplesChoiceWithTableRepWithLineFeed: P[Example] = (endOfLine.rep0.void.with1 *> examplesChoiceWithTable).backtrack

  private[parser] val scenarioOutline: P[FeatureElement.ScenarioOutline] =
    (scenarioOutlineHeader ~ stepRepWithLineFeed ~ examplesChoiceWithTableRepWithLineFeed.rep).map {
      case ((scenarioOutlineHeader, steps), examples) =>
        ScenarioOutline(
          name = scenarioOutlineHeader,
          steps = steps,
          examples = examples,
          tags = Nil)
    }
}
