Feature: Current user retrieves his own permissions
  Scenario: Current user list his permissions
    Given user account EGAW0000005 with email amohan@ebi.ac.uk exist
    And datasets belongs to DAC EGAC0000001
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 |
    And datasets belongs to DAC EGAC0000002
      | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And user account EGAW0000005 has permissions to datasets
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And user account EGAW0000005 has a valid token
    When user account EGAW0000005 list permissions for himself
    Then response should have status code 200
    And response should only contain items
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |

  Scenario: Current user doesn't have any permissions
    Given user account EGAW0000005 with email amohan@ebi.ac.uk exist
    And datasets belongs to DAC EGAC0000001
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 |
    And user account EGAW0000005 has a valid token
    When user account EGAW0000005 list permissions for himself
    Then response should have status code 200
    And response should not contain any items
