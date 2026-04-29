ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS status VARCHAR(32);

UPDATE vehicles SET status = 'available' WHERE status IS NULL OR trim(status) = '';

DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'vehicles'
      AND column_name = 'maintenance'
  ) THEN
    UPDATE vehicles SET status = 'maintenance' WHERE maintenance = TRUE;
  END IF;
END $$;

UPDATE vehicles SET status = 'available' WHERE status IS NULL OR trim(status) = '';

ALTER TABLE vehicles ALTER COLUMN status SET DEFAULT 'available';

ALTER TABLE vehicles ALTER COLUMN status SET NOT NULL;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'vehicles'
      AND column_name = 'maintenance'
  ) THEN
    ALTER TABLE vehicles DROP COLUMN maintenance;
  END IF;
END $$;
