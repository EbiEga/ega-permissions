CREATE SCHEMA IF NOT EXISTS pea;

CREATE TABLE IF NOT EXISTS pea.account (
	account_id varchar(128) NOT NULL,
	first_name varchar(256) NULL,
	last_name varchar(256) NULL,
	email varchar(128) NULL,
	status varchar(60) NULL
);

CREATE OR REPLACE VIEW account
AS SELECT acc.account_id,
    acc.first_name,
    acc.last_name,
    acc.email,
    acc.status
FROM pea.account as acc;
