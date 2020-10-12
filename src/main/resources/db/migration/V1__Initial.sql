-- public.passport_claim definition

-- Drop table

-- DROP TABLE public.passport_claim;

CREATE TYPE visa_type AS ENUM
('AffiliationAndRole', 'ControlledAccessGrants',
'AcceptedTermsAndPolicies','ResearcherStatus',
'LinkedIdentities');

CREATE TYPE visa_authority AS ENUM
('dac', 'system');

CREATE TABLE passport_claim (
	account_id varchar(255) NOT NULL,
	value varchar(255) NOT NULL,
	asserted int8 NULL,
	by VISA_AUTHORITY,
	source varchar(255) NULL,
	type VISA_TYPE,
	CONSTRAINT passport_claim_pkey PRIMARY KEY (account_id, value)
);

CREATE TABLE account_elixir_id (
	account_id varchar(128) NOT NULL,
	elixir_id varchar(128) NOT NULL,
	elixir_email varchar(255) NOT NULL
);

CREATE INDEX passport_claim_account_idx ON passport_claim(account_id);