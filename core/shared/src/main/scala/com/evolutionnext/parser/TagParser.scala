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
import cats.parse.Rfc5234.wsp
import com.evolutionnext.gherkin.Tag
import com.evolutionnext.parser.CatsParseSupport.newline

object TagParser {
  private val tagName: P[String] =
    (P.charWhere(ch => !ch.isWhitespace && ch != '@') ~
      P.charsWhile0(ch => !ch.isWhitespace && ch != '@')).map {
      case (head, tail) => s"$head$tail"
    }

  private[parser] val tag: P[Tag] =
    (P.char('@') *> tagName).map(Tag.apply)

  private[parser] val tagLine: P[List[Tag]] =
    wsp.rep0.void.with1 *> (tag.repSep(P.char(' ')) <* newline.?).map(_.toList)
}
