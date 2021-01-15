CREATE TABLE IF NOT EXISTS event (
	event_id int8 NOT NULL auto_increment,
	bearer_id varchar(255) NOT NULL,
	user_id varchar(255) NOT NULL,
	method varchar(7) NOT NULL,
	data text NULL,
	created timestamp NOT NULL DEFAULT now()
);
