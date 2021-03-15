Feature: Current user retrieves his own permissions

  Scenario: Current user list his permissions
    Given user EGAW0000005 with email amohan@ebi.ac.uk exist
    And datasets belong to DAC EGAC0000001
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 |
    And datasets belong to DAC EGAC0000002
      | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And user EGAW0000005 has access to datasets
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And user acquires a valid token
    When user lists permissions for himself
    Then response has status code 200
    And response only contains items
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |

  Scenario: Current user doesn't have any permissions
    Given user account EGAW0000005 with email amohan@ebi.ac.uk exist
    And datasets belong to DAC EGAC0000001
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 |
    And user acquires a valid token
    When user lists permissions for himself
    Then response has status code 200
    And response does not contain any items
