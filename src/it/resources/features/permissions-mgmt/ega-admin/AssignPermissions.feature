Feature: EGA Admin Assign permissions to user

  Scenario: EGA Admin grants permissions to user
    Given EGA Admin EGAW0000001 with email amohan@ebi.ac.uk exists
    And user EGAW0000002 with email test@ebi.ac.uk exist
    And dataset EGAD00001 belongs to DAC EGAC0000001
    And user acquires a valid token
    When user account EGAW0000001 grants permissions to account EGAW0000002 on dataset EGAD00001
    Then response has status code 207
    And dataset has status code 201

  Scenario: EGA Admin with invalid token fails to grant permissions to user
    Given EGA Admin EGAW0000001 with email amohan@ebi.ac.uk exists
    And user EGAW0000002 with email test@ebi.ac.uk exist
    And dataset EGAD00001 belongs to DAC EGAC0000001
    And user has an invalid token
    When user account EGAW0000001 grants permissions to account EGAW0000002 on dataset EGAD00001
    Then response has status code 401