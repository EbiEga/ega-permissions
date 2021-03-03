Feature: DAC Admin Retrieve permissions
  Scenario Outline: DAC Admin with read permissions to datasets list permissions
    Given user account EGAW0000001 with email amohan@ebi.ac.uk exist
    And user account EGAW0000002 with email test@ebi.ac.uk exist
    And datasets belongs to DAC EGAC0000001
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 |
    And datasets belongs to DAC EGAC0000003
      | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And account EGAW0000001 is linked to DAC EGAC0000001 with <level> permissions
    And user account EGAW0000002 has permissions to datasets
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And user account EGAW0000001 has a valid token
    When user account EGAW0000001 list permissions for account EGAW0000002
    Then response should have status code 200
    And response should only contain items
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 |

    Examples:
    | level |
    | read  |
    | write |

  Scenario: DAC Admin with invalid token tries to list permissions
    Given user account EGAW0000001 with email amohan@ebi.ac.uk exist
    And user account EGAW0000002 with email test@ebi.ac.uk exist
    And datasets belongs to DAC EGAC0000001
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 |
    And datasets belongs to DAC EGAC0000003
      | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And account EGAW0000001 is linked to DAC EGAC0000001 with <level> permissions
    And user account EGAW0000002 has permissions to datasets
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And user account EGAW0000001 has an invalid token
    When user account EGAW0000001 list permissions for account EGAW0000002
    Then response should have status code 401