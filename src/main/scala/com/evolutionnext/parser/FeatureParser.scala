package com.evolutionnext.parser

import cats.parse.Rfc5234.*
import cats.parse.{Parser as P, Parser0 as P0}
import com.evolutionnext.gherkin.*
import com.evolutionnext.gherkin.FeatureElement.{Scenario, ScenarioOutline}

object FeatureParser:

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

  private val nonNewLineP: P0[Unit] = P.not(crlf)

  val restOfLine: P[String] =
    ((alpha | wsp).rep.string <* (crlf | cr | lf)).withContext("restOfLine")

  private val text: P[String] =
    (P.charWhere(nonNewline) ~ P.charsWhile0(nonNewline)).map {
      case (head, tail) => s"$head$tail"
    }

  private val ignorable: P0[Unit] =
    (crlf | cr | lf | wsp).rep0.void

  private[parser] val featureHeader: P[Feature] =
    (P.string("Feature:") *> spaces0 *> text <* newline.?).map(s =>
      Feature(Nil, s, Nil, None, Nil)).withContext("featureHeader")

  private val scenarioHeader: P[String] =
    (P.string("Scenario:") *> spaces0 *> text <* newline.?).map(_.trim).withContext("scenarioHeader")

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

  private val tags: P0[List[Tag]] =
    tagLine.rep0.map(_.flatten)

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

  private val scenario: P[Scenario] =
    taggedScenario.backtrack.orElse(untaggedScenario).withContext("scenario")

  val scenarioOutlineHeader: P[ScenarioOutline] =
    wsp.rep0.void.with1 *> (P.string("Scenario Outline:") *> restOfLine)
      .map(_.trim)
      .map(s => ScenarioOutline(Nil, s, Nil, Nil))

  val examples: P[Example] =
    (
      wsp.rep0.void.with1 *> P.string("Examples:").void *> wsp
        .rep0.void *> endOfLine *> table <* (cr | lf).rep0.void
    ).map(Example.apply).withContext("examples")

  val scenarioOutline: P[FeatureElement.ScenarioOutline] =
    (scenarioOutlineHeader ~ step.rep ~ examples.rep)
      .map {
        case ((scenarioOutline, steps), examples) =>
          scenarioOutline.copy(steps = steps.toList, examples = examples.toList)
      }
      .withContext("ScenarioOutlineWithSteps")

  private val featureElement: P[FeatureElement] =
    ignorable.with1 *> scenarioOutline.orElse(scenario)

  val feature: P[Feature] =
    (tagLine.rep.?.with1 ~ featureHeader ~ featureElement.rep <* (cr | lf).rep.? <* P.end).map {
      case ((maybeTagLines, feature), featureElements) =>
        val listTags: List[Tag] = maybeTagLines match {
          case Some(tags) => tags.toList.flatten
          case None => Nil
        }
        feature.copy(tags = listTags, featureElements = featureElements.toList)
    }

  def parse(content: String): Either[P.Error, Feature] =
    val normalized =
      content.linesIterator.map(_.trim).filter(_.nonEmpty).mkString("\n") + "\n"
    feature.parseAll(normalized)
