Feature: DAC Admin Revokes permissions from user

  Scenario: DAC Admin with read/write permissions to Dataset revokes permissions from user
    Given EGA Admin EGAW0000001 with email amohan@ebi.ac.uk exists
    And user EGAW0000002 with email test@ebi.ac.uk exist
    And datasets belong to DAC EGAC0000001
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 |
    And datasets belong to DAC EGAC0000003
      | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And user EGAW0000002 has access to datasets
      | EGAD00001 | EGAD00002 | EGAD00003 | EGAD00004 | EGAD00005 | EGAD00006 | EGAD00007 | EGAD00008 | EGAD00009 | EGAD00010 |
    And user acquires a valid token
    When admin revokes all from user account EGAW0000002
    Then response has status code 200
    And list permissions for account EGAW0000002
    And response has status code 404