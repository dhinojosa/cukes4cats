package com.evolutionnext.parser
import cats.parse.Parser as P
import cats.parse.Rfc5234.*
import com.evolutionnext.gherkin.StepArgument.DocString
import com.evolutionnext.parser.CatsParseSupport.{endOfLineOrInput, spaces0, text}

object DocStringParser {
  private val docStringNoMediaTypeStartAnchor: P[Option[String]] =
    (spaces0.void.with1 *> P.string("\"\"\"").void *> wsp.rep0.void *> (cr | lf).rep.void).map(
      _ => None)

  private val docStringMediaTypeStartAnchor: P[Option[String]] =
    (spaces0.void.with1 *> P.string("\"\"\"").void *> text <* wsp.rep0.void *> (cr | lf)
      .rep
      .void).map(s => Some(s))

  private val docStringEndAnchor: P[Unit] =
    (wsp.rep0.void.with1 *> P.string("\"\"\"") <* wsp.rep0.void) <* endOfLineOrInput

  private[parser] val docStringOld: P[DocString] =
    (docStringMediaTypeStartAnchor
      .backtrack
      .orElse(docStringNoMediaTypeStartAnchor) ~ P.anyChar.rep.string <* docStringEndAnchor)
      .map { case (maybeMediaType, text) => DocString(text, maybeMediaType) }

  private[parser] val docString: P[DocString] =
    (
      wsp.rep0.collect { case xs => xs.length }.with1 ~ docStringMediaTypeStartAnchor
        .backtrack
        .orElse(docStringNoMediaTypeStartAnchor) ~
        P.until0(docStringEndAnchor) <*
        docStringEndAnchor
    ).map {
      case ((spaceCount, maybeMediaType), content) =>
        DocString(prepareString(spaceCount, content), maybeMediaType)
    }

  private def prepareString(count:Int, content: String): String = {
      val lineSeparator = System.lineSeparator()
      content.split("[\n\r]").map(_.substring(count)).mkString(lineSeparator)
  }
}
