Feature: Manage Access Groups

  Scenario: EGA Admin add user to Access Group
    Given EGA Admin EGAW0000001 with email amohan@ebi.ac.uk exists
    And user EGAW0000002 with email test@ebi.ac.uk exist
    And user acquires a valid token
    When add user EGAW0000002 to access group EGAC0000001 with write permission
    Then response has status code 200
    And database contains access group EGAC0000001 for user EGAW0000002 with write permission

  Scenario: EGA Admin retrieves Users assigned to Access Group
    Given EGA Admin EGAW0000001 with email amohan@ebi.ac.uk exists
    And DAC Admin user EGAW0000002 with email test2@ebi.ac.uk and read access to EGAC0000001 exist
    And DAC Admin user EGAW0000003 with email test3@ebi.ac.uk and write access to EGAC0000001 exist
    And user acquires a valid token
    When retrieve users for group EGAC0000001
    Then response has status code 200
    And response only contains group users
      | EGAW0000002 | EGAW0000003 |

  Scenario: EGA Admin removes one Access Group from user
    Given EGA Admin EGAW0000001 with email amohan@ebi.ac.uk exists
    And DAC Admin user EGAW0000002 with email test2@ebi.ac.uk and read access to EGAC0000001 exist
    And DAC Admin user EGAW0000003 with email test3@ebi.ac.uk and write access to EGAC0000001 exist
    And user acquires a valid token
    When remove group EGAC0000001 from user EGAW0000002
    Then response has status code 200
    And database does not contain group EGAC0000001 for user EGAW0000002

  Scenario: EGA Admin removes ALL Access Groups from user
    Given EGA Admin EGAW0000001 with email amohan@ebi.ac.uk exists
    And DAC Admin user EGAW0000002 with email test2@ebi.ac.uk and read access to EGAC0000001 exist
    And DAC Admin user EGAW0000003 with email test3@ebi.ac.uk and write access to EGAC0000001 exist
    And user acquires a valid token
    When remove group all from user EGAW0000002
    Then response has status code 200
    And database does not contain group EGAC0000001 for user EGAW0000002

  Scenario: EGA Admin retrieves Access Groups assigned to User
    Given EGA Admin EGAW0000001 with email amohan@ebi.ac.uk exists
    And DAC Admin user EGAW0000002 with email test2@ebi.ac.uk and read access to EGAC0000001 exist
    And DAC Admin user EGAW0000003 with email test3@ebi.ac.uk and write access to EGAC0000002 exist
    And user acquires a valid token
    When retrieve groups for user EGAW0000002
    Then response has status code 200
    And response only contains access groups
      | EGAC0000001 |

  Scenario: Current user retrieve his Access Groups
    Given DAC Admin user EGAW0000001 with email amohan@ebi.ac.uk and read access to EGAC0000001 exist
    And user acquires a valid token
    When retrieve current user Access Groups
    Then response has status code 200
    And response only contains access groups
      | EGAC0000001 |