package com.evolutionnext.parser

import cats.parse.Parser as P
import cats.parse.Rfc5234.*
import com.evolutionnext.gherkin.{FeatureElement, Rule}
import com.evolutionnext.parser.CatsParseSupport.*
import com.evolutionnext.parser.ScenarioOutlineParser.scenarioOutline
import com.evolutionnext.parser.ScenarioParser.scenario

object RuleParser {
  private val ruleString: P[String] =
    wsp.rep0.void.with1 *> P.string("Rule:").void *> wsp.rep0 *> text <* wsp.rep0.void

  /*
   * There is an ordering restriction: all feature-level scenarios must appear before the first Rule.
   * Once a Rule begins, you cannot return to feature-level scenarios.
   */
  private val ruleElement: P[FeatureElement] =
    scenarioOutline.backtrack.orElse(scenario)

  private[parser] val rule: P[Rule] =
    ((ruleString <* (cr | lf).rep.void) ~ ruleElement.rep).map {
      case (title, featureElements) =>
        Rule(title, featureElements)
    }
}
