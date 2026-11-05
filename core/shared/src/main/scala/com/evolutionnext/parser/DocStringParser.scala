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
import com.evolutionnext.gherkin.StepArgument.DocString
import com.evolutionnext.parser.CatsParseSupport.{endOfLineOrInput, text}

object DocStringParser {
  private val docStringNoMediaTypeStartAnchor: P[Option[String]] =
    (P.string("\"\"\"").void *> wsp.rep0.void *> (cr | lf).rep.void).map(_ => None)

  private val docStringMediaTypeStartAnchor: P[Option[String]] =
    (P.string("\"\"\"").void *> text <* wsp.rep0.void *> (cr | lf).rep.void).map(s => Some(s))

  private val docStringEndAnchor: P[Unit] =
    (wsp.rep0.void.with1 *> P.string("\"\"\"").withContext("end delimiter") <* wsp
      .rep0
      .void) <* endOfLineOrInput

  private[parser] val docString: P[DocString] =
    (
      wsp.rep0.map(_.length).with1 ~ docStringMediaTypeStartAnchor
        .backtrack
        .orElse(docStringNoMediaTypeStartAnchor) ~
        P.until0(docStringEndAnchor) <*
        docStringEndAnchor
    ).map {
      case ((spaceCount, maybeMediaType), content) =>
        DocString(prepareString(spaceCount, content), maybeMediaType)
    }

  private def prepareString(count: Int, content: String): String = {
    content
      .split("[\n\r]")
      .flatMap(line => normalizeLine(count, line))
      .mkString(System.lineSeparator())
  }

  private def normalizeLine(count: Int, content: String): Option[String] = {
    println(s"count: $count\tcontent:$content")
    if (content.contains("\r") || content.contains("\n")) Option.empty[String]
    else {
      val findFirstCharacter = content.indexWhere(_.isLetterOrDigit)
      Option(content.drop(math.min(count, findFirstCharacter)))
    }
  }
}
