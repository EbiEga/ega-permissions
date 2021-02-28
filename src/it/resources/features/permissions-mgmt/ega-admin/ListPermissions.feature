Feature: EGA Admin Retrieve permissions
  Scenario: EGA Admin list permissions
    Given user account EGAW0000001 with email amohan@ebi.ac.uk exist
    And user account EGAW0000002 with email test@ebi.ac.uk exist
    And datasets belongs to DAC EGAC0000001
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 |
    And datasets belongs to DAC EGAC0000002
      | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And account EGAW0000001 is an EGA Admin
    And user account EGAW0000002 has permissions to datasets
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And user account EGAW0000001 has a valid token
    When user account EGAW0000001 list permissions for account EGAW0000002
    Then response should have status code 200 and only contain
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |