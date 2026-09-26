Feature: Password policy

    Rule: Password must be at least 8 characters

        Scenario: Reject short password
            Given a password of "short"
            When I validate the password
            Then the password should be rejected

    Rule: Password must contain a number

        Scenario: Reject password without number
            Given a password of "longpassword"
            When I validate the password
            Then the password should be rejected
