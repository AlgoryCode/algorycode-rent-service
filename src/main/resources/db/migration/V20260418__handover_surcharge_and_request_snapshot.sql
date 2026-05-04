DO $migration$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'handover_locations'
  ) THEN
    ALTER TABLE handover_locations
      ADD COLUMN IF NOT EXISTS surcharge_eur DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
  END IF;
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'rental_requests'
  ) THEN
    ALTER TABLE rental_requests
      ADD COLUMN IF NOT EXISTS handover_pickup_leg_eur DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
      ADD COLUMN IF NOT EXISTS handover_return_leg_eur DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
      ADD COLUMN IF NOT EXISTS handover_route_eur DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
      ADD COLUMN IF NOT EXISTS handover_total_eur DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
  END IF;
END $migration$;
