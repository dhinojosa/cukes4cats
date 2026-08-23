package com.evolutionnext.gherkin

case class Background(
    name: Option[String],
    steps: List[Step]
)

final case class Feature(
    tags: List[Tag],
    name: String,
    description: List[String],
    background: Option[Background],
    scenarios: List[Scenario]
)

final case class Scenario(
    tags: List[Tag],
    name: String,
    steps: List[Step]
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

enum StepKeyword {
  case Given, When, Then, And, But
}
