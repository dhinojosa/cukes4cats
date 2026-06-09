# Cukes4Cats

Cukes4Cats is a functional Behavior-Driven Development framework built on Cats Effect.

Unlike traditional BDD frameworks that rely on reflection, dependency injection containers, and mutable scenario state, Cukes4Cats treats a scenario as a typed program.

## Design Goals

- Parse Gherkin into a strongly-typed AST
- Compile scenarios into Cats programs
- Use explicit scenario state instead of hidden mutable state
- Avoid reflection and runtime introspection
- Integrate naturally with MUnit, Weaver, and ScalaCheck
- Enable multiple proof strategies (Unit, Integration, API, UI)
- Produce rich, extensible reports

## Architecture

```text
.feature file
      ↓
 cats-parse
      ↓
 Gherkin AST
      ↓
 Glue Registry
      ↓
 StateT Program
      ↓
 Cats Effect IO
      ↓
 MUnit / Weaver
```

## Scenario State

A scenario is represented as a stateful program.

```scala
type Step[F[_]] =
  StateT[F, ScenarioContext, Unit]
```

The scenario state is explicit and passed to the program when executed.

```scala
program.runS(initialContext)
```

No dependency injection container is required.

## Typed Glue

```scala
Given[Int]("I have number {int}") { n =>
  StepF.modify(_.copy(left = Some(n)))
}

When[Int]("I add number {int}") { n =>
  StepF.modify(ctx =>
    ctx.copy(result = ctx.left.map(_ + n))
  )
}

Then[Int]("the result is {int}") { expected =>
  StepF.inspectF(ctx =>
    IO(assertEquals(ctx.result, Some(expected)))
  )
}
```

or perhaps better

```scala 3
val glue =
  Glue[IO]
    .given[Int]("I have number {int}") { n =>
      StepF.modify(_.copy(left = Some(n)))
    }
    .when[Int]("I add number {int}") { n =>
      StepF.modify(ctx => ctx.copy(result = ctx.left.map(_ + n)))
    }
    .then[Int]("the result is {int}") { expected =>
      StepF.inspectF(ctx => IO(assertEquals(ctx.result, Some(expected))))
    }
```



The framework is responsible for:

- Matching step expressions
- Extracting parameters
- Decoding values
- Executing handlers

The user works entirely with typed values.

## Parsing

```text
Feature
 └── Scenario
      └── Step
```

The parser owns syntax.

The glue registry owns meaning.

The runner owns execution.

## Compilation

```text
Step AST
   ↓
Glue Lookup
   ↓
Step[F]
   ↓
StateT[F, ScenarioContext, Unit]
```

## Reporting

Execution emits events.

```scala
enum CukeEvent:
  case FeatureStarted(...)
  case ScenarioStarted(...)
  case StepStarted(...)
  case StepPassed(...)
  case StepFailed(...)
  case ScenarioFinished(...)
  case FeatureFinished(...)
```

Reporters subscribe to the event stream.

```scala
trait Reporter[F[_]] {
  def publish(event: CukeEvent): F[Unit]
}
```

Examples:

- Console Reporter
- HTML Reporter
- Markdown Reporter
- JSON Reporter
- JUnit XML Reporter

Reporters can be composed.

```scala
Reporter.combine(
  ConsoleReporter,
  HtmlReporter,
  JsonReporter
)
```

## Proofs

Scenarios may be executed against multiple proof implementations.

- Unit Proof
- Property-Based Proof
- Integration Proof
- API Proof
- UI Proof

Rather than using the Composite Pattern, proofs are composed functionally.

```scala
proofs.traverse_(_.run(scenario))
```

## Philosophy

Traditional Cucumber:

```text
Gherkin
  ↓
Reflection
  ↓
Methods
```

Cukes4Cats:

```text
Gherkin
  ↓
AST
  ↓
Program
  ↓
Interpreter
```

> The scenario is not a collection of methods.
>
> The scenario is a program.

### Possible Runner for MUnit

```scala 3
class ArithmeticFeatureSuite extends CatsEffectSuite {
  test("basic arithmetic") {
    CukeRunner.runFeature[IO](
      featurePath = "features/arithmetic.feature",
      glue = ArithmeticGlue,
      initial = ScenarioContext.empty
    ).void
  }
}
```

### Possible Runner for Weaver

```scala 3
object ArithmeticFeatureSuite extends SimpleIOSuite {
  pureTest("basic arithmetic") {
    CukeRunner.runFeature[IO](
      featurePath = "features/arithmetic.feature",
      glue = ArithmeticGlue,
      initial = ScenarioContext.empty
    ).as(success)
  }
}
```
