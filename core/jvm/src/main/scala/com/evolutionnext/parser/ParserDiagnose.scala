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

import cats.parse.Parser

object ParserDiagnose {
  def visibleWhitespace(s: String): String =
    s.replace(" ", "␠").replace("\t", "␉").replace("\r", "␍").replace("\n", "␊")

  def betterMessage(e: Parser.Error): String = {
    e.input.map { s =>
      val visible = visibleWhitespace(s)
      val offset = e.expected.head.offset

      val start = math.max(0, offset - 10)
      val end = math.min(visible.length, offset + 11)

      if offset < visible.length then {
        val before = visible.slice(start, offset)
        val char = visible(offset)
        val after = visible.slice(offset + 1, end)

        s"$before❯$char❮$after"
      } else {
        s"${visible.slice(start, offset)}❯EOF❮"
      }
    }.get
  }
}
