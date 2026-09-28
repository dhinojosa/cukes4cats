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
import com.evolutionnext.parser.CatsParseSupport.{endOfLineOrInput, spaces0, text}

object DocStringParser {
  private val docStringNoMediaTypeStartAnchor: P[Option[String]] =
    (spaces0.void.with1 *> P.string("\"\"\"").void *> wsp.rep0.void *> (cr | lf).rep.void).map(
      _ => None)

  private val docStringMediaTypeStartAnchor: P[Option[String]] =
    (spaces0.void.with1 *> P.string("\"\"\"").void *> text <* wsp.rep0.void *> (cr | lf)
      .rep
      .void).map(s => Some(s))

  private val docStringEndAnchor: P[Unit] =
    (wsp.rep0.void.with1 *> P.string("\"\"\"") <* wsp.rep0.void) <* endOfLineOrInput

  private[parser] val docStringOld: P[DocString] =
    (docStringMediaTypeStartAnchor
      .backtrack
      .orElse(docStringNoMediaTypeStartAnchor) ~ P.anyChar.rep.string <* docStringEndAnchor)
      .map { case (maybeMediaType, text) => DocString(text, maybeMediaType) }

  private[parser] val docString: P[DocString] =
    (
      wsp.rep0.collect { case xs => xs.length }.with1 ~ docStringMediaTypeStartAnchor
        .backtrack
        .orElse(docStringNoMediaTypeStartAnchor) ~
        P.until0(docStringEndAnchor) <*
        docStringEndAnchor
    ).map {
      case ((spaceCount, maybeMediaType), content) =>
        DocString(prepareString(spaceCount, content), maybeMediaType)
    }

  private def prepareString(count: Int, content: String): String = {
    val lineSeparator = System.lineSeparator()
    content.split("[\n\r]").map(_.substring(count)).mkString(lineSeparator)
  }
}
