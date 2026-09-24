package com.evolutionnext.gherkin

import cats.data.NonEmptyList
import cats.parse.Parser.Expectation

enum CukeError {
  case IOError(message: String)
  case ParserError(message: String, expectation: NonEmptyList[Expectation])
  case RunnerError(message: String)
}
