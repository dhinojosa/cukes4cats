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

package com.evolutionnext.parser.docstring

import cats.data.NonEmptyList
import cats.parse.Parser
import cats.parse.Parser.Expectation.{OneOfStr, WithContext}
import munit.ScalaCheckSuite

class DocStringErrorSuite extends ScalaCheckSuite {
  test("extract an opening delimiter error") {
    val parserError = Parser.Error(
      3,
      NonEmptyList.one(
        WithContext(
          DocStringError.OpeningDelimiterContext,
          OneOfStr(3, List("\"\"\""))
        )
      )
    )

    assertEquals(
      DocStringError.unapply(parserError),
      Some(DocStringError.ExpectedOpeningDelimiter)
    )
  }

  test("extract an unterminated Doc String error from any expectation") {
    val parserError = Parser.Error(
      12,
      NonEmptyList.of(
        OneOfStr(4, List("content")),
        WithContext(
          DocStringError.ClosingDelimiterContext,
          OneOfStr(12, List("\"\"\""))
        )
      )
    )

    assertEquals(
      DocStringError.unapply(parserError),
      Some(DocStringError.Unterminated)
    )
  }

  test("do not extract an unrelated parser error") {
    val parserError =
      Parser.Error(3, NonEmptyList.one(OneOfStr(3, List("unrelated"))))

    assertEquals(DocStringError.unapply(parserError), None)
  }
}
