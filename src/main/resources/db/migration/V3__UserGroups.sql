
CREATE TYPE access_group AS ENUM (
	'EGAAdmin',
	'DAC',
	'User');

CREATE TYPE access_level AS ENUM (
	'read',
	'write');

CREATE TABLE user_group (
	user_id varchar(255) NOT NULL,
	group_id varchar(255) NOT NULL,
	access_group access_group,
	access_level access_level,
	CONSTRAINT user_group_pkey PRIMARY KEY (user_id, group_id)
);