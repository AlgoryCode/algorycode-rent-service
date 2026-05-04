DO $migration$
BEGIN
  IF to_regclass('public.rental_requests') IS NULL THEN
    RETURN;
  END IF;
  ALTER TABLE rental_requests ADD COLUMN IF NOT EXISTS start_time TIME NULL;
  ALTER TABLE rental_requests ADD COLUMN IF NOT EXISTS return_time TIME NULL;
END $migration$;
