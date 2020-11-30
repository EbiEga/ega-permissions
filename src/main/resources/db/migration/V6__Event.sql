CREATE SEQUENCE event_id_seq
    start 1
    increment 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS event (
	event_id int8 NOT NULL DEFAULT nextval('event_id_seq'),
	bearer_id varchar(255) NOT NULL,
	user_id varchar(255) NOT NULL,
	method varchar(7) NOT NULL,
	data text NULL,
	created timestamp NOT NULL DEFAULT now()
);
