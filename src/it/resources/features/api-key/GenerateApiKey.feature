Feature: Generate API Key
  Scenario: User generates API Key
    Given user account EGAW0000001 with email amohan@ebi.ac.uk exist
    And user account EGAW0000001 has a valid token
    When user account EGAW0000001 request a new Token with key My Test Token
    Then a response containing the token key My Test Token is returned