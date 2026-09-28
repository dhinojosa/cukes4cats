package com.evolutionnext.parser
import cats.parse.Parser as P
import cats.parse.Rfc5234.*
import com.evolutionnext.gherkin.StepArgument.DataTable
import com.evolutionnext.gherkin.{Step, StepKeyword}
import com.evolutionnext.parser.CatsParseSupport.{newline, text}
import com.evolutionnext.parser.DocStringParser.docString
import com.evolutionnext.parser.TableParser.table

object StepParser {
  private def stepLine(prefix: String, keyword: StepKeyword): P[Step] = {
    val stepPrefix: P[String] = wsp.rep0.void.with1 *> P.string(prefix) *> P.char(' ') *> text

    val stepWithTable: P[Step] =
      ((stepPrefix <* newline) ~ table).map {
        case (stepText, parsedTable) =>
          Step(keyword, stepText.trim, Option(DataTable(parsedTable)))
      }

    val stepWithDocString: P[Step] =
      ((stepPrefix <* newline) ~ docString).map {
        case (stepText, docString) =>
          Step(keyword = keyword, text = stepText.trim, stepArgument = Option(docString))
      }

    val stepWithoutTable: P[Step] =
      (stepPrefix <* newline.?).map { stepText => Step(keyword, stepText.trim, None) }

    stepWithTable.backtrack.orElse(stepWithDocString.backtrack).orElse(stepWithoutTable)
  }

  private[parser] val step: P[Step] =
    stepLine("Given", StepKeyword.Given)
      .backtrack
      .orElse(stepLine("When", StepKeyword.When).backtrack)
      .orElse(stepLine("Then", StepKeyword.Then).backtrack)
      .orElse(stepLine("And", StepKeyword.And).backtrack)
      .orElse(stepLine("But", StepKeyword.But))
}
