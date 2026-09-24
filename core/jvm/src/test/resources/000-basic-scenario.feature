Feature: Basic arithmetic

    Scenario: Add two numbers
        Given the number 2
        And the number 3
        When I add the numbers
        Then the result should be 5
