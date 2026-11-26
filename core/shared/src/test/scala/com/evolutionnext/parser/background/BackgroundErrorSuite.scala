package com.evolutionnext.parser.background

import cats.data.NonEmptyList
import cats.parse.Parser
import cats.parse.Parser.Expectation.{OneOfStr, WithContext}
import munit.FunSuite

import scala.language.postfixOps

class BackgroundErrorSuite extends FunSuite {
  test("convert a background-steps Parser.Error to an appropriate BackgroundError") {
    val parserError: Parser.Error =
      Parser.Error(3, NonEmptyList.one(WithContext("background-steps", OneOfStr(2, Nil))))
    val maybeBackgroundError: Option[BackgroundError] = BackgroundError.unapply(parserError)
    maybeBackgroundError match {
      case Some(backgroundError: BackgroundError) =>
        assertEquals(backgroundError, BackgroundError.MissingSteps)
      case None => fail("background steps not legitimately parsed")
    }
  }

  test("convert a undefined Parser.Error to a BackgroundError") {
    val parserError: Parser.Error =
      Parser.Error(3, NonEmptyList.one(OneOfStr(2, Nil)))
    val backgroundError: Option[BackgroundError] = BackgroundError.unapply(parserError)
    assert(backgroundError.isEmpty)
  }
}
