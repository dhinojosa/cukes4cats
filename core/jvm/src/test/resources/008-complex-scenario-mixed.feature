@billing @important
Feature: Invoice generation

    Background:
        Given an empty invoice
        And tax rate is 10 percent

    Rule: Invoice totals should include tax

        @smoke
        Scenario Outline: Generate invoice from line items
            Given the invoice contains:
                | description | quantity | unitPrice |
                | <item>      | <qty>    | <price>   |
            When I generate the invoice
            Then the subtotal should be <subtotal>
            And the tax should be <tax>
            And the total should be <total>

            Examples:
                | item     | qty | price | subtotal | tax  | total |
                | Book     | 2   | 15.00 | 30.00    | 3.00 | 33.00 |
                | Pen      | 4   |  2.50 | 10.00    | 1.00 | 11.00 |
                | Notebook | 1   | 20.00 | 20.00    | 2.00 | 22.00 |
