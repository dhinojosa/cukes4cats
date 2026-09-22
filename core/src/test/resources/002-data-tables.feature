Feature: Bulk user registration

    Scenario: Register several users
        Given the following users exist:
            | username | email              | role   |
            | alice    | alice@example.com  | admin  |
            | bob      | bob@example.com    | editor |
            | carol    | carol@example.com  | viewer |
        When I list all users
        Then I should see 3 users
