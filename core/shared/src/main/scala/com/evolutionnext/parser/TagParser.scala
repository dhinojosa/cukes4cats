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
