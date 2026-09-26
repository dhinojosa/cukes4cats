# This feature verifies parser tolerance for comments and spacing

@parser
Feature: Comments and whitespace

  # Scenario comment
    Scenario: Ignore comments correctly
        Given a starting value of 10

    # Intermediate comment
        When I increment the value

        Then the result should be 11
