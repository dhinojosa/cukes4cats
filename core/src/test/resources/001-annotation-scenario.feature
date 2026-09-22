@fast @unit
Feature: Tagged arithmetic

    @smoke
    Scenario: Simple addition
        Given the number 1
        And the number 1
        When I add the numbers
        Then the result should be 2

    @slow @integration
    Scenario: Large addition
        Given the number 1000
        And the number 2000
        When I add the numbers
        Then the result should be 3000
