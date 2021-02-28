Feature: DAC Admin Assign permissions to user
  Scenario Outline: DAC Admin with read/write permissions to Dataset grants permissions to another user
    Given user account <dac_user> with email amohan@ebi.ac.uk exist
    And user account <ega_user> with email test@ebi.ac.uk exist
    And dataset <dataset_id> belongs to DAC <dac_of_dataset>
    And account <dac_user> is linked to DAC <dac_of_dac_user> with <level> permissions
    And user account <dac_user> has a valid token
    When user account <dac_user> grants permissions to account <ega_user> on dataset <dataset_id>
    Then the response status is <response_status> and the dataset status is <dataset_status>

    Examples:
      | dac_user    | ega_user    | dataset_id  | dac_of_dataset  | dac_of_dac_user | level | response_status | dataset_status  |
      | EGAW0000001 | EGAW0000002 | EGAD00003   | EGAC0000003     | EGAC0000003     | read  | 401             | 0               |
      | EGAW0000001 | EGAW0000002 | EGAD00001   | EGAC0000001     | EGAC0000001     | write | 207             | 201             |
      | EGAW0000001 | EGAW0000002 | EGAD00002   | EGAC0000002     | EGAC0000001     | write | 207             | 401             |
