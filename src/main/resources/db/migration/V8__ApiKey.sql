CREATE SEQUENCE apikey_id_seq
    start 1
    increment 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS api_key (
	username varchar(255) NOT NULL,
	key_name varchar(255) NOT NULL,
	expiration timestamp NOT NULL,
	reason varchar(255) NOT NULL,
	salt varchar(255) NOT NULL,
	private_key text NOT NULL,
	CONSTRAINT api_key_pkey PRIMARY KEY (username, key_name)
);