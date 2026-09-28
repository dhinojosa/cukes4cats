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
