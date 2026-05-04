DO $migration$
BEGIN
  IF to_regclass('public.vehicles') IS NULL THEN
    RETURN;
  END IF;
  ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS fuel_type VARCHAR(64) NULL;
  ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS body_color VARCHAR(64) NULL;
END $migration$;
