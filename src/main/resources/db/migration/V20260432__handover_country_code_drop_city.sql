DO $migration$
BEGIN
  IF to_regclass('public.handover_locations') IS NOT NULL THEN
    ALTER TABLE handover_locations ADD COLUMN IF NOT EXISTS country_code VARCHAR(64);
  END IF;
END $migration$;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'handover_locations'
      AND column_name = 'city_id'
  ) THEN
    UPDATE handover_locations hl
    SET country_code = co.code
    FROM cities c
    JOIN countries co ON co.id = c.country_id
    WHERE hl.city_id IS NOT NULL
      AND hl.city_id = c.id
      AND (hl.country_code IS NULL OR btrim(hl.country_code::text) = '');

    ALTER TABLE handover_locations DROP COLUMN city_id CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'vehicles'
      AND column_name = 'city_id'
  ) THEN
    ALTER TABLE vehicles DROP CONSTRAINT IF EXISTS fk_vehicles_city;
    DROP INDEX IF EXISTS idx_vehicles_city_id;
    ALTER TABLE vehicles DROP COLUMN city_id CASCADE;
  END IF;
END $$;
