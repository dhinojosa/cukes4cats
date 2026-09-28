package com.evolutionnext.parser
import cats.parse.Parser as P
import cats.parse.Rfc5234.*
import com.evolutionnext.gherkin.Background
import com.evolutionnext.parser.StepParser.step

object BackgroundParser {
  private val backgroundHeader: P[Unit] =
    wsp.rep0.void.with1 *> P.string("Background:").void *> wsp.rep0.void *> (cr | lf).void

  private[parser] val background: P[Background] =
    (backgroundHeader.void *> step.rep).map(steps => Background(steps))
}
