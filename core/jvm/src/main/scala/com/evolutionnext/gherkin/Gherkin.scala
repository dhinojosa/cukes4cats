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

enum FeatureElement:
  case ScenarioOutline(
      tags: List[Tag],
      name: String,
      steps: NonEmptyList[Step],
      examples: NonEmptyList[Example]
  )

  case Scenario(
      tags: List[Tag],
      name: String,
      steps: List[Step]
  )

enum StepKeyword {
  case Given, When, Then, And, But
}
