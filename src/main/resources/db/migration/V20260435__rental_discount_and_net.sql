DO $migration$
BEGIN
  IF to_regclass('public.rentals') IS NULL THEN
    RETURN;
  END IF;
  ALTER TABLE rentals ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
  ALTER TABLE rentals ADD COLUMN IF NOT EXISTS discount_type VARCHAR(16);
  ALTER TABLE rentals ADD COLUMN IF NOT EXISTS net_amount NUMERIC(12,2);
END $migration$;
