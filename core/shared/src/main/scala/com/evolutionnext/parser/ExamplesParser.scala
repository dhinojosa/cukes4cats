package com.evolutionnext.parser
import cats.parse.Parser as P
import cats.parse.Rfc5234.*
import com.evolutionnext.gherkin.Example
import com.evolutionnext.parser.CatsParseSupport.{endOfLine, text}
import com.evolutionnext.parser.TableParser.table

object ExamplesParser {
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

  private[parser] val examplesChoice: P[Option[String]] =
    exampleLine.backtrack | exampleLineWithLabel

  private[parser] val examplesChoiceWithTable: P[Example] =
    (examplesChoice ~ table).map { case (label, table) => Example(label, table) }
}
