-- DROP TYPE access_group;

CREATE TYPE access_group AS ENUM (
	'EGAAdmin',
	'DAC',
	'User');

-- DROP TYPE access_level;

CREATE TYPE access_level AS ENUM (
	'read',
	'write');

-- user_group definition

-- Drop table

-- DROP TABLE public.user_group;

CREATE TABLE user_group (
	source_account_id varchar(255) NOT NULL,
	destination_account_id varchar(255) NOT NULL,
	group_id access_group,
	level_id access_level,
	CONSTRAINT user_group_pkey PRIMARY KEY (source_account_id, destination_account_id)
);


