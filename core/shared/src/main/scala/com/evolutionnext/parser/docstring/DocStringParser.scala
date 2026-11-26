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

package com.evolutionnext.parser.docstring
import cats.parse.Rfc5234.*
import cats.parse.{Parser0, Parser as P}
import com.evolutionnext.gherkin.StepArgument.DocString
import com.evolutionnext.parser.CatsParseSupport.{endOfLineOrInput, text}

object DocStringParser {

  val endOfLine: P[Unit] = crlf.backtrack.orElse(cr).orElse(lf)

  private val delimiter = "\"\"\""

  private val nonNewline: Char => Boolean =
    ch => ch != '\r' && ch != '\n'

  private val contentLine: P[String] =
    P.charsWhile0(nonNewline).with1 <* endOfLine

  private val closingLine: P[Unit] =
        (wsp.rep0.with1 *> P.string(delimiter) <* wsp.rep0 <* endOfLineOrInput).void

  private val contentLines: Parser0[List[String]] =
    contentLine.repUntil0(closingLine) <* closingLine

  private val openingIndentation: Parser0[Int] = wsp.rep0.map(_.length)

  private val openingDelimiter: P[Unit] = P.string(delimiter).void

  private val optionalMediaType: Parser0[Option[String]] = text.?


  val openingLine: P[(Int, Option[String])] =
    openingIndentation.with1 ~ (openingDelimiter *> optionalMediaType <* endOfLine)

  val docString: P[DocString] =
    (openingLine ~ contentLines).map {
      case ((margin, mediaType), lines) =>
        DocString(
          lines.map(line => normalizeLine(margin, line)).mkString("\n"),
          mediaType
        )
    }

  private def normalizeLine(count: Int, content: String): String = {
    val leadingWhitespace =
      content.segmentLength(ch => ch == ' ' || ch == '\t')

    content.drop(math.min(count, leadingWhitespace))
  }
}
