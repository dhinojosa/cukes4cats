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
