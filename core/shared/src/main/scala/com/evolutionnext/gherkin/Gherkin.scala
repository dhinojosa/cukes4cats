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

package com.evolutionnext.gherkin

import cats.data.NonEmptyList

case class Background(
    steps: NonEmptyList[Step]
)

final case class Feature(
    tags: List[Tag],
    name: String,
    background: Option[Background],
    featureElements: List[FeatureElement],
    rules: List[Rule]
)

final case class Tag(value: String)

final case class Step(
    keyword: StepKeyword,
    text: String,
    table: Option[Table] = Option.empty[Table]
)
final case class Cell(
    string: String
)
final case class Row(
    cells: Cell*
)

final case class Header(
    cells: Cell*
)

final case class Table(
    row: Row*
)

final case class Rule(
    text: String,
    featureElements: NonEmptyList[FeatureElement]
)

case class Example(label: Option[String], table: NonEmptyList[Table])

sealed trait FeatureElement extends Product with Serializable

object FeatureElement {
  final case class ScenarioOutline(
      tags: List[Tag],
      name: String,
      steps: NonEmptyList[Step],
      examples: NonEmptyList[Example]
  ) extends FeatureElement

  final case class Scenario(
      tags: List[Tag],
      name: String,
      steps: List[Step]
  ) extends FeatureElement
}

sealed trait StepKeyword extends Product with Serializable

object StepKeyword {
  case object Given extends StepKeyword
  case object When extends StepKeyword
  case object Then extends StepKeyword
  case object And extends StepKeyword
  case object But extends StepKeyword
}
