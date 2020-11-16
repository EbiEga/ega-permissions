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


-- Add function
CREATE OR REPLACE FUNCTION fn_update_set_timestamp()
  RETURNS TRIGGER
  LANGUAGE PLPGSQL
  as
$$
BEGIN
	new.timestamp = now();
	return new;
END;
$$;

-- Add trigger to passport_claim
CREATE TRIGGER on_update_passport_claim_set_timestamp
    AFTER UPDATE ON passport_claim
    FOR EACH ROW
    EXECUTE function fn_update_set_timestamp();

-- Add trigger to user_group
CREATE TRIGGER on_update_user_group_set_timestamp
    AFTER UPDATE ON user_group
    FOR EACH ROW
    EXECUTE function fn_update_set_timestamp();
