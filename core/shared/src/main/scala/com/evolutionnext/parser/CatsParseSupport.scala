package com.evolutionnext.parser
import cats.parse.Rfc5234.*
import cats.parse.{Parser as P, Parser0 as P0}

object CatsParseSupport {

  private[parser] val ignorable: P0[Unit] =
    (crlf | cr | lf | wsp).rep0.void

  private[parser] val spaces0: P0[Unit] = wsp.rep0.void

  private val nonNewline: Char => Boolean =
    ch => ch != '\n' && ch != '\r'

  private[parser] val text: P[String] =
    (P.charWhere(nonNewline) ~ P.charsWhile0(nonNewline)).map {
      case (head, tail) => s"$head$tail"
    }
  private[parser] val newline: P[Unit] =
    P.string("\r\n").void.orElse(P.char('\n').void)

  private[parser] val endOfLine: P[Unit] =
    P.string("\r\n").void.orElse(P.char('\n').void).orElse(P.char('\r').void)

  private[parser] val endOfLineOrInput: P0[Unit] =
    endOfLine.orElse(P.end)
}
