DO $migration$
BEGIN
  IF to_regclass('public.vehicles') IS NOT NULL THEN
    ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS fe_fleet_snapshot JSON NULL;
  END IF;
END $migration$;
