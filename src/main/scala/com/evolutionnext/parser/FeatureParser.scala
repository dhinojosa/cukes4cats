package com.evolutionnext.parser

import cats.parse.{Parser => P, Parser0 => P0}
import com.evolutionnext.gherkin.{Feature, Scenario, Step, StepKeyword}

object FeatureParser:

    private val newline: P[Unit] =
        P.string("\r\n").void.orElse(P.char('\n').void)

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

    private def stepLine(prefix: String, keyword: StepKeyword): P[Step] =
        (P.string(prefix) *> P.char(' ') *> text <* newline.?).map { stepText =>
            Step(keyword, stepText.trim)
        }

    private val step: P[Step] =
        stepLine("Given", StepKeyword.Given)
            .backtrack
            .orElse(stepLine("When", StepKeyword.When).backtrack)
            .orElse(stepLine("Then", StepKeyword.Then).backtrack)
            .orElse(stepLine("And", StepKeyword.And).backtrack)
            .orElse(stepLine("But", StepKeyword.But))

    private val scenario: P[Scenario] =
        (scenarioHeader ~ step.rep).map { case (name, steps) =>
            Scenario(
                tags = Nil,
                name = name,
                steps = steps.toList
            )
        }

    private val feature: P[Feature] =
        (featureHeader ~ scenario.rep).map { case (name, scenarios) =>
            Feature(
                tags = Nil,
                name = name,
                description = Nil,
                scenarios = scenarios.toList
            )
        }

    def parse(content: String): Feature =
        val normalized =
            content
                .linesIterator
                .map(_.trim)
                .filter(_.nonEmpty)
                .mkString("\n")

        feature.parseAll(normalized) match
            case Right(value) => value
            case Left(error) =>
                throw new IllegalArgumentException(s"Could not parse feature: $error")