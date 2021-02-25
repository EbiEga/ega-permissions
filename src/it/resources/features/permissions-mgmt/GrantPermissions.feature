Feature: permissions are granted to user
  Scenario: EGA Admin grants permissions to user
    Given user account EGAW0000001 with email amohan@ebi.ac.uk exist
    And user account EGAW0000002 with email test@ebi.ac.uk exist
    And dataset EGAD00001 belongs to DAC EGAC0000001
    And account EGAW0000001 is an EGA Admin
    And user account EGAW0000001 has a valid token
    When user account EGAW0000001 grants permissions to account EGAW0000002 on dataset EGAD00001
    Then the response status is 207 and the dataset status is 201

  Scenario Outline: DAC with write permission to Dataset grants permissions to another user
    Given user account <dac_user> with email amohan@ebi.ac.uk exist
    And user account <ega_user> with email test@ebi.ac.uk exist
    And dataset <dataset_id> belongs to DAC <dac_of_dataset>
    And account <dac_user> is linked to DAC <dac_of_dac_user> with <level> permissions
    And user account <dac_user> has a valid token
    When user account <dac_user> grants permissions to account <ega_user> on dataset <dataset_id>
    Then the response status is <response_status> and the dataset status is <dataset_status>

    Examples:
      | dac_user    | ega_user    | dataset_id  | dac_of_dataset  | dac_of_dac_user | level | response_status | dataset_status  |
      | EGAW0000001 | EGAW0000002 | EGAD00001   | EGAC0000001     | EGAC0000001     | write | 207             | 201             |
#      | EGAW0000001 | EGAW0000002 | EGAD00001   | EGAC0000001     | EGAC0000001     | read  | 207             | 401             |
#      | EGAW0000001 | EGAW0000002 | EGAD00001   | EGAC0000002     | EGAC0000001     | write | 207             | 401             |
#      | EGAW0000001 | EGAW0000002 | EGAD00001   | EGAC0000001     | EGAC0000002     | write | 207             | 401             |