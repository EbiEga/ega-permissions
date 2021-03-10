Feature: Generate API Key

  Scenario: User generates API Key
    Given user EGAW0000001 with email amohan@ebi.ac.uk exist
    And user acquires a valid token
    When user request a new Token with key My Test Token
    Then response has status code 200
    And response contains the token key My Test Token

  Scenario: User try to generate API Key with invalid token
    Given user EGAW0000001 with email amohan@ebi.ac.uk exist
    And user has an invalid token
    When user request a new Token with key My Test Token
    Then response has status code 401