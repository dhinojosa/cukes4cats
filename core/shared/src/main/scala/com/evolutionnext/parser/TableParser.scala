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
