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

  val endOfLine: P[Unit] = cr.backtrack.orElse(lf).backtrack.orElse(crlf)

  private val docStringEndAnchor: P[Unit] = {
    ((endOfLine ~ wsp.rep0).void *> P.string("\"\"\"").withContext("end delimiter") <* wsp
      .rep0
      .void) <* endOfLineOrInput
  }

  private val openingIndentation: Parser0[Int] = wsp.rep0.map(_.length)

  private val openingDelimiter: P[Unit] = P.string("\"\"\"").void

  private val optionalMediaType: Parser0[Option[String]] = text.?

  val openingLine: P[(Int, Option[String])] =
    openingIndentation.with1 ~ (openingDelimiter *> optionalMediaType <* endOfLine)

  val docString: P[DocString] =
    (openingLine ~ P.until0(docStringEndAnchor) <* docStringEndAnchor).map {
      case ((margin, mediaType), rawContent) =>
        DocString(
          prepareString(margin, rawContent),
          mediaType
        )
    }

  private def prepareString(count: Int, content: String): String = {
    content
      .split("[\n\r]")
      .flatMap(line => normalizeLine(count, line))
      .mkString(System.lineSeparator())
  }

  private def normalizeLine(count: Int, content: String): Option[String] = {
    if (content.contains("\r") || content.contains("\n")) Option.empty[String]
    else {
      val findFirstCharacter = content.segmentLength(Character.isWhitespace)
      Option(content.drop(math.min(count, findFirstCharacter)))
    }
  }
}
