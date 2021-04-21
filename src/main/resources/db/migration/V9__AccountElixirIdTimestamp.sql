IF NOT EXISTS( SELECT NULL
            FROM INFORMATION_SCHEMA.COLUMNS
           WHERE table_name = 'account_elixir_id'
             AND table_schema = 'pea'
             AND column_name = 'created_at')  THEN

  ALTER TABLE pea.account_elixir_id ADD COLUMN created_at timestamp DEFAULT now();

END IF;


CREATE OR REPLACE VIEW account_elixir_id
AS SELECT ae.account_id,
    ae.elixir_id,
    ae.elixir_email,
    ae.created_at
FROM pea.account_elixir_id ae;