-- Rename user_group table columns

ALTER TABLE user_group
  RENAME COLUMN user_id TO ega_account_stable_id;

ALTER TABLE user_group
  RENAME COLUMN group_id TO group_stable_id;

ALTER TABLE user_group
  RENAME COLUMN access_group TO group_type;

ALTER TABLE user_group
  RENAME COLUMN access_level TO permission;
