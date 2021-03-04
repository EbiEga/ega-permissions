Feature: EGA Admin Assign permissions to user
  Scenario: EGA Admin grants permissions to user
    Given user account EGAW0000001 with email amohan@ebi.ac.uk exist
    And user account EGAW0000002 with email test@ebi.ac.uk exist
    And dataset EGAD00001 belongs to DAC EGAC0000001
    And account EGAW0000001 is an EGA Admin
    And user account EGAW0000001 has a valid token
    When user account EGAW0000001 grants permissions to account EGAW0000002 on dataset EGAD00001
    Then the response status is 207 and the dataset status is 201

  Scenario: EGA Admin with invalid token fails to grant permissions to user
    Given user account EGAW0000001 with email amohan@ebi.ac.uk exist
    And user account EGAW0000002 with email test@ebi.ac.uk exist
    And dataset EGAD00001 belongs to DAC EGAC0000001
    And account EGAW0000001 is an EGA Admin
    And user account EGAW0000001 has an invalid token
    When user account EGAW0000001 grants permissions to account EGAW0000002 on dataset EGAD00001
    Then response should have status code 401