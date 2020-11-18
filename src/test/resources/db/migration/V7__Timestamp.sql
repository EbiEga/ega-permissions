-- Modify passport_claim table
ALTER TABLE passport_claim
ADD COLUMN timestamp timestamp DEFAULT now();

ALTER TABLE passport_claim
ADD COLUMN status varchar(50) DEFAULT 'approved';

-- Modify user_group table
ALTER TABLE user_group
ADD COLUMN timestamp timestamp DEFAULT now();

ALTER TABLE user_group
ADD COLUMN status varchar(50) DEFAULT 'approved';

ALTER TABLE user_group
ADD COLUMN pea_record int8 default 0;

-- Skip on creating functions and triggers since H2 doesn't support those