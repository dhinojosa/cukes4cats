Feature: Shipping cost

    Scenario Outline: Shipping rates vary by region
        Given a package weight of <weight>
        And a destination region of "<region>"
        When I calculate shipping
        Then the shipping cost should be <cost>

        Examples: Domestic
            | weight | region   | cost |
            | 1      | US       | 5.00 |
            | 5      | US       | 8.50 |

        Examples: International
            | weight | region   | cost |
            | 1      | EU       | 12.00 |
            | 5      | APAC     | 20.00 |
