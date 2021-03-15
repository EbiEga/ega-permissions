Feature: DAC Admin Retrieve permissions

  Scenario Outline: DAC Admin with read permissions to datasets list permissions
    Given DAC Admin user EGAW0000001 with email amohan@ebi.ac.uk and <level> access to EGAC0000001 exist
    And user EGAW0000002 with email test@ebi.ac.uk exist
    And datasets belong to DAC EGAC0000001
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 |
    And datasets belong to DAC EGAC0000003
      | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And user EGAW0000002 has access to datasets
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And user acquires a valid token
    When list permissions for account EGAW0000002
    Then response has status code 200
    And response only contains items
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 |

    Examples:
      | level |
      | read  |
      | write |

  Scenario: DAC Admin with invalid token tries to list permissions
    Given DAC Admin user EGAW0000001 with email amohan@ebi.ac.uk and read access to EGAC0000001 exist
    And user EGAW0000002 with email test@ebi.ac.uk exist
    And datasets belong to DAC EGAC0000001
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 |
    And datasets belong to DAC EGAC0000003
      | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And user EGAW0000002 has access to datasets
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And user has an invalid token
    When list permissions for account EGAW0000002
    Then response has status code 401