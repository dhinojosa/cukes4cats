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
import com.evolutionnext.gherkin.{Cell, Row, Table}
import com.evolutionnext.parser.CatsParseSupport.*

object TableParser {
  private val pipe: P[Unit] = P.char('|').void

  private val cellText: P[String] =
    P.charsWhile(ch => ch != '|' && ch != '\n' && ch != '\r').map(_.trim)

  private val cell: P[Cell] = (cellText <* pipe).map(Cell.apply)

  private val row: P[Row] =
    (wsp.rep0.void.with1 *> pipe *> cell.rep <* endOfLineOrInput)
      .map(cells => Row(cells.toList: _*))
      .withContext("row")

  private[parser] val table: P[Table] =
    row.backtrack.rep.map(rows => Table(rows.toList: _*)).withContext("table")
}
