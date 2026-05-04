DO $migration$
BEGIN
  IF to_regclass('public.countries') IS NOT NULL THEN
    ALTER TABLE countries ALTER COLUMN code TYPE VARCHAR(5);
    ALTER TABLE countries ALTER COLUMN code SET NOT NULL;
  END IF;
  IF to_regclass('public.vehicles') IS NOT NULL THEN
    ALTER TABLE vehicles ALTER COLUMN country_code TYPE VARCHAR(5);
  END IF;
END $migration$;
