CREATE SCHEMA IF NOT EXISTS pea;

CREATE TABLE IF NOT EXISTS pea.account_elixir_id (
	account_id varchar(128) NULL,
	elixir_id varchar(128) NULL,
	elixir_email varchar(256) NULL
);

CREATE OR REPLACE VIEW account_elixir_id
AS SELECT ae.account_id,
    ae.elixir_id,
    ae.elixir_email
FROM pea.account_elixir_id ae;
