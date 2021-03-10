Feature: Generate API Key

  Scenario: User generates API Key
    Given user EGAW0000001 with email amohan@ebi.ac.uk exist
    And user acquires a valid token
    When user request API_KEY Token TEST1
    Then response has status code 200
    And response contains token TEST1

  Scenario: User try to generate API Key with invalid token
    Given user EGAW0000001 with email amohan@ebi.ac.uk exist
    And user has an invalid token
    When user request API_KEY Token TEST1
    Then response has status code 401