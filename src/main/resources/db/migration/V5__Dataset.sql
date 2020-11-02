CREATE SCHEMA IF NOT EXISTS pea;

CREATE TABLE IF NOT EXISTS pea.dataset (
	dataset_id varchar(128) NOT NULL,
	description text NULL,
	dac_stable_id varchar(128) NULL,
	double_signature varchar(3) NULL
);

CREATE OR REPLACE VIEW dataset
AS SELECT ds.dataset_id,
    ds.description,
    ds.dac_stable_id,
    ds.double_signature
FROM pea.dataset as ds;
