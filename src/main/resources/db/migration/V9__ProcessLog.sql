CREATE TABLE process_log (
    process varchar(255) NOT NULL,
    event varchar(255) NOT NULL,
    timestamp timestamp DEFAULT CURRENT_TIMESTAMP,
    additional_data text NULL
);