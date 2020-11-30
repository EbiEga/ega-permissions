
CREATE TYPE group_type AS ENUM (
	'EGAAdmin',
	'DAC',
	'User');

CREATE TYPE permission AS ENUM (
	'read',
	'write');

CREATE TABLE user_group (
	ega_account_stable_id varchar(255) NOT NULL,
	group_stable_id varchar(255) NOT NULL,
	group_type group_type,
	permission permission,
	CONSTRAINT user_group_pkey PRIMARY KEY (ega_account_stable_id, group_stable_id)
);