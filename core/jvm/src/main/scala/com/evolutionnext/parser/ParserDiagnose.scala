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
