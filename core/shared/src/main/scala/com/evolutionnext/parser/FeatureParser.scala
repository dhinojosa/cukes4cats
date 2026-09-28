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

import cats.parse.Rfc5234.*
import cats.parse.{Parser as P, Parser0 as P0}
import com.evolutionnext.gherkin.*
import com.evolutionnext.parser.BackgroundParser.background
import com.evolutionnext.parser.CatsParseSupport.*
import com.evolutionnext.parser.RuleParser.rule
import com.evolutionnext.parser.ScenarioOutlineParser.scenarioOutline
import com.evolutionnext.parser.ScenarioParser.scenario
import com.evolutionnext.parser.TagParser.tagLine

object FeatureParser {

  private[parser] val featureHeader: P[String] =
    ignorable.with1 *> (P.string("Feature:") *> spaces0 *> text <* newline.?)
      .withContext("featureHeader")

  private val featureElement: P[FeatureElement] =
    endOfLine.rep0.void.with1 *> scenarioOutline.backtrack.orElse(scenario)

  private val ruleTrimmed: P0[List[Rule]] =
    (endOfLine.rep0.void.with1 *> rule).backtrack.rep0

  private val backgroundTrimmed: P0[Option[Background]] =
    (endOfLine.rep0.void.with1 *> background).backtrack.?

  val feature: P[Feature] =
    (tagLine.rep.?.with1 ~
      featureHeader ~
      backgroundTrimmed ~
      featureElement.backtrack.rep0 ~ ruleTrimmed <* (cr | lf).rep.? <* P.end).map {
      case ((((maybeTagLines, featureHeader), maybeBackground), featureElements), rules) =>
        val listTags: List[Tag] = maybeTagLines match {
          case Some(tags) => tags.toList.flatten
          case None => Nil
        }
        Feature(listTags, featureHeader, maybeBackground, featureElements, rules)
    }

  def parse(content: String): Either[P.Error, Feature] = {
    feature.parseAll(content)
  }
}
