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
