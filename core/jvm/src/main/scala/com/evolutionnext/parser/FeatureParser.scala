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
import com.evolutionnext.gherkin.FeatureElement.{Scenario, ScenarioOutline}

object FeatureParser {

  private val newline: P[Unit] =
    P.string("\r\n").void.orElse(P.char('\n').void)

  private val pipe: P[Unit] = P.char('|').void

  private val cellText: P[String] =
    P.charsWhile(ch => ch != '|' && ch != '\n' && ch != '\r').map(_.trim)

  private val cell: P[Cell] = (cellText <* pipe).map(Cell.apply)

  private val row: P[Row] =
    (pipe *> cell.rep).map(cells => Row(cells.toList *)).withContext("row")

  private val endOfLine: P[Unit] =
    P.string("\r\n").void.orElse(P.char('\n').void).orElse(P.char('\r').void)

  private val table: P[Table] =
    (row <* newline).rep.map { rows => Table(rows.toList *) }.withContext("table")

  private val spaces0: P0[Unit] = wsp.rep0.void

  private val nonNewline: Char => Boolean =
    ch => ch != '\n' && ch != '\r'

  val restOfLine: P[String] =
    ((alpha | wsp).rep.string <* (crlf | cr | lf)).withContext("restOfLine")

  private val text: P[String] =
    (P.charWhere(nonNewline) ~ P.charsWhile0(nonNewline)).map {
      case (head, tail) => s"$head$tail"
    }

  private val ignorable: P0[Unit] =
    (crlf | cr | lf | wsp).rep0.void

  private[parser] val featureHeader: P[String] =
    (P.string("Feature:") *> spaces0 *> text <* newline.?).withContext("featureHeader")

  private val scenarioHeader: P[String] =
    (P.string("Scenario:") *> spaces0 *> text <* newline.?)
      .map(_.trim)
      .withContext("scenarioHeader")

  private def stepLine(prefix: String, keyword: StepKeyword): P[Step] = {
    val stepPrefix = ignorable.with1 *> P.string(prefix) *> P.char(' ') *> text

    val stepWithTable: P[Step] =
      ((stepPrefix <* newline) ~ table).map {
        case (stepText, parsedTable) =>
          Step(keyword, stepText.trim, Some(parsedTable))
      }

    val stepWithoutTable: P[Step] =
      (stepPrefix <* newline.?).map { stepText => Step(keyword, stepText.trim, None) }

    stepWithTable.backtrack.orElse(stepWithoutTable)
  }

  private val step: P[Step] =
    stepLine("Given", StepKeyword.Given)
      .backtrack
      .orElse(stepLine("When", StepKeyword.When).backtrack)
      .orElse(stepLine("Then", StepKeyword.Then).backtrack)
      .orElse(stepLine("And", StepKeyword.And).backtrack)
      .orElse(stepLine("But", StepKeyword.But))

  private val tagName: P[String] =
    (P.charWhere(ch => !ch.isWhitespace && ch != '@') ~
      P.charsWhile0(ch => !ch.isWhitespace && ch != '@')).map {
      case (head, tail) => s"$head$tail"
    }

  private val tag: P[Tag] =
    (P.char('@') *> tagName).map(Tag.apply)

  private val tagLine: P[List[Tag]] =
    (tag.repSep(P.char(' ')) <* newline.?).map(_.toList)

  private val taggedScenario: P[Scenario] =
    (tagLine.rep ~ scenarioHeader ~ step.rep).map {
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

  private val backgroundHeader: P[Unit] =
    wsp.rep0.void.with1 *> P.string("Background:").void *> wsp.rep0.void *> (cr | lf).void

  private val background: P[Background] =
    (backgroundHeader.void *> step.rep).map(steps => Background(steps))

  private val scenario: P[Scenario] =
    taggedScenario.backtrack.orElse(untaggedScenario).withContext("scenario")

  private val scenarioOutlineHeader: P[String] =
    wsp.rep0.void.with1 *> (P.string("Scenario Outline:") *> restOfLine).map(_.trim)

  private val exampleLine: P[Option[String]] =
    (wsp.rep0.void.with1 *> P.string("Examples:").void *>
      wsp.rep0.void *> endOfLine)
      .map(u => Option.empty[String])
      .withContext("Example Line Without Label")

  private val exampleLineWithLabel: P[Option[String]] =
    (wsp.rep0.void.with1 *> P.string("Examples:").void *>
      wsp.rep0.void *> text <* wsp.rep0.void <* endOfLine)
      .map(s => Option(s))
      .withContext("Example Line With Label")

  private val examplesChoice: P[Option[String]] = exampleLine.backtrack | exampleLineWithLabel

  private val examplesChoiceWithTable: P[Example] =
    (examplesChoice ~ table.rep).map(Example.apply)

  private val scenarioOutline: P[FeatureElement.ScenarioOutline] =
    (scenarioOutlineHeader ~ step.rep ~ examplesChoiceWithTable.rep).map {
      case ((scenarioOutlineHeader, steps), examples) =>
        ScenarioOutline(
          name = scenarioOutlineHeader,
          steps = steps,
          examples = examples,
          tags = Nil)
    }

  private val ruleString: P[String] =
    ignorable.with1 *> P.string("Rule:").void *> wsp.rep0 *> text <* wsp.rep0.void *> (cr | lf)
      .void
      .rep

  /*
   * There is an ordering restriction: all feature-level scenarios must appear before the first Rule.
   * Once a Rule begins, you cannot return to feature-level scenarios.
   */

  val rule: P[Rule] = (ruleString ~ (scenario.backtrack | scenarioOutline).rep).map {
    (title, featureElements) => Rule(title, featureElements)
  }

  private val featureElement: P[FeatureElement] =
    ignorable.with1 *> scenarioOutline.orElse(scenario)

  val feature: P[Feature] =
    (tagLine
      .rep
      .?
      .with1 ~ featureHeader ~ background.? ~ featureElement.rep0 ~ rule.rep0 <* (cr | lf)
      .rep
      .? <* P.end).map {
      case ((((maybeTagLines, featureHeader), maybeBackground), featureElements), rules) =>
        val listTags: List[Tag] = maybeTagLines match {
          case Some(tags) => tags.toList.flatten
          case None => Nil
        }
        Feature(listTags, featureHeader, maybeBackground, featureElements, rules)
    }

  def parse(content: String): Either[P.Error, Feature] =
    val normalized =
      content.linesIterator.map(_.trim).filter(_.nonEmpty).mkString("\n") + "\n"
    feature.parseAll(normalized)
}
