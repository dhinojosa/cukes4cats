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

import com.evolutionnext.gherkin.FeatureElement.ScenarioOutline
import munit.FunSuite

class ScenarioOutlineParserSuite extends FunSuite {
  test("test a scenario outline") {
    val text: String =
      """Scenario Outline: Apply discount to total
        |    Given a subtotal of <subtotal>
        |    When I apply a discount of <discountPercent> percent
        |    Then the final total should be <total>
        |
        |    Examples:
        |        | subtotal | discountPercent | total |
        |        | 100.00   | 10              | 90.00 |
        |        | 80.00    | 25              | 60.00 |
        |        | 50.00    | 0               | 50.00 |""".stripMargin

    val parseResult = ScenarioOutlineParser.scenarioOutline.parseAll(text)
    parseResult match {
      case Right(scenarioOutline) =>
        assertEquals(scenarioOutline.name, "Apply discount to total")
      case Left(parseError) => fail(ParserDiagnose.betterMessage(parseError))
    }
  }
}
