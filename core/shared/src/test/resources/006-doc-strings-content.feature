Feature: JSON validation

    Scenario: Submit a JSON payload
        Given the request body:
           """json
           {
             "name": "Ada",
             "email": "ada@example.com",
             "active": true
           }
           """
        When I submit the request
        Then the response status should be 200
