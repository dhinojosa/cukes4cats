package com.evolutionnext.parser

import com.evolutionnext.gherkin.Tag
import munit.FunSuite

class TagParserSuite extends FunSuite {
  test("parse a single tag") {
    val text = "@food"
    val result = TagParser.tag.parseAll(text)
    result match {
      case Right(tag) => assertEquals(tag, Tag("food"))
      case Left(parseError) => fail(ParserDiagnose.betterMessage(parseError))
    }
  }

  test("parse a tag list") {
    val text = "@food @drink"
    val result = TagParser.tagLine.parseAll(text)
    result match {
      case Right(list) => assertEquals(list, List(Tag("food"), Tag("drink")))
      case Left(parseError) => fail(ParserDiagnose.betterMessage(parseError))
    }
  }

  test("parse a tag list with space indentation") {
    val text = "           @food @drink"
    val result = TagParser.tagLine.parseAll(text)
    result match {
      case Right(list) => assertEquals(list, List(Tag("food"), Tag("drink")))
      case Left(parseError) => fail(ParserDiagnose.betterMessage(parseError))
    }
  }

  test("parse a tag list with tag indentation") {
    val text = "\t\t\t@food @drink"
    val result = TagParser.tagLine.parseAll(text)
    result match {
      case Right(list) => assertEquals(list, List(Tag("food"), Tag("drink")))
      case Left(parseError) => fail(ParserDiagnose.betterMessage(parseError))
    }
  }
}
