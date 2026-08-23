package com.evolutionnext.parser

import cats.parse.{Parser as P, Parser0 as P0}
import com.evolutionnext.gherkin.*

object FeatureParser:

  private val newline: P[Unit] =
    P.string("\r\n").void.orElse(P.char('\n').void)

  private val pipe: P[Unit] = P.char('|').void

  private val cellText: P[String] =
    P.charsWhile(ch => ch != '|' && ch != '\n' && ch != '\r').map(_.trim)

  private val cell: P[Cell] = (cellText <* pipe).map(Cell.apply)

  private val row: P[Row] =
    (pipe *> cell.rep).map(cells => Row(cells.toList *))

  private val endOfLine: P[Unit] =
    P.string("\r\n").void.orElse(P.char('\n').void).orElse(P.char('\r').void)

  private val table: P[Table] =
    (row <* newline).rep.map { rows => Table(rows.toList *) }

  private val spaces0: P0[Unit] =
    P.charIn(" \t").rep0.void

  private val nonNewline: Char => Boolean =
    ch => ch != '\n' && ch != '\r'

  private val text: P[String] =
    (P.charWhere(nonNewline) ~ P.charsWhile0(nonNewline)).map {
      case (head, tail) => s"$head$tail"
    }

  private val featureHeader: P[String] =
    (P.string("Feature:") *> spaces0 *> text <* newline.?).map(_.trim)

  private val scenarioHeader: P[String] =
    (P.string("Scenario:") *> spaces0 *> text <* newline.?).map(_.trim)

  private def stepLine(prefix: String, keyword: StepKeyword): P[Step] = {
    val stepPrefix = P.string(prefix) *> P.char(' ') *> text

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
    taggedScenario.backtrack.orElse(untaggedScenario)

  private val featureWithTags: P[Feature] =
    (tagLine.rep ~ featureHeader ~ scenario.rep).map {
      case ((tagLines, name), scenarios) =>
        Feature(
          tags = tagLines.toList.flatten,
          name = name,
          background = None,
          description = Nil,
          scenarios = scenarios.toList
        )
    }

  private val featureWithoutTags: P[Feature] =
    (featureHeader ~ scenario.rep).map {
      case (name, scenarios) =>
        Feature(
          tags = Nil,
          name = name,
          background = None,
          description = Nil,
          scenarios = scenarios.toList
        )
    }

  private val feature: P[Feature] =
    featureWithTags.backtrack.orElse(featureWithoutTags)

  def parse(content: String): Either[P.Error, Feature] =
    val normalized =
      content.linesIterator.map(_.trim).filter(_.nonEmpty).mkString("\n") + "\n"
    feature.parseAll(normalized)
