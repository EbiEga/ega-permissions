-- public.passport_claim definition

-- Drop table

-- DROP TABLE public.passport_claim;

CREATE TABLE public.passport_claim (
	account_id varchar(255) NOT NULL,
	value varchar(255) NOT NULL,
	asserted int4 NULL,
	by varchar(255) NULL,
	source varchar(255) NULL,
	type varchar(255) NULL,
	CONSTRAINT passport_claim_pkey PRIMARY KEY (account_id, value)
);