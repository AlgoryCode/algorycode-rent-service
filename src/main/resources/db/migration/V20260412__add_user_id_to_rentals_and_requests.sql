DO $migration$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'rentals'
  ) THEN
    ALTER TABLE rentals ADD COLUMN IF NOT EXISTS user_id UUID NULL;
    CREATE INDEX IF NOT EXISTS idx_rentals_user_id ON rentals (user_id);
  END IF;
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'rental_requests'
  ) THEN
    ALTER TABLE rental_requests ADD COLUMN IF NOT EXISTS user_id UUID NULL;
    CREATE INDEX IF NOT EXISTS idx_rental_requests_user_id ON rental_requests (user_id);
  END IF;
END $migration$;
