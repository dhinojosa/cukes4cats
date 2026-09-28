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
