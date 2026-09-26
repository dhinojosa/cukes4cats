Feature: Shopping cart

    Background:
        Given an empty shopping cart
        And a catalog with the following items:
            | sku   | name       | price |
            | A100  | Notebook   | 10.00 |
            | B200  | Pencil     |  2.50 |

    Scenario: Add one item
        When I add item "A100" to the cart
        Then the cart should contain 1 item
        And the total should be 10.00

    Scenario: Add two items
        When I add item "A100" to the cart
        And I add item "B200" to the cart
        Then the cart should contain 2 items
        And the total should be 12.50
