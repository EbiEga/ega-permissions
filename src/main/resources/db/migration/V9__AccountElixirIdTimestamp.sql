ALTER TABLE pea.account_elixir_id ADD COLUMN created_at timestamp DEFAULT now();

CREATE OR REPLACE VIEW account_elixir_id
AS SELECT ae.account_id,
    ae.elixir_id,
    ae.elixir_email,
    ae.created_at
FROM pea.account_elixir_id ae;