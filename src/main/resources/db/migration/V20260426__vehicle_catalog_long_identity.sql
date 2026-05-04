DO $migration$
BEGIN
  IF to_regclass('public.vehicle_body_styles') IS NOT NULL
     AND NOT EXISTS (
       SELECT 1 FROM information_schema.columns
       WHERE table_schema = 'public' AND table_name = 'vehicle_body_styles' AND column_name = 'id'
     ) THEN
    ALTER TABLE vehicle_body_styles DROP CONSTRAINT vehicle_body_styles_pkey;
    ALTER TABLE vehicle_body_styles ADD COLUMN id BIGSERIAL PRIMARY KEY;
    ALTER TABLE vehicle_body_styles ADD CONSTRAINT uq_vehicle_body_styles_code UNIQUE (code);
  END IF;

  IF to_regclass('public.vehicle_fuel_types') IS NOT NULL
     AND NOT EXISTS (
       SELECT 1 FROM information_schema.columns
       WHERE table_schema = 'public' AND table_name = 'vehicle_fuel_types' AND column_name = 'id'
     ) THEN
    ALTER TABLE vehicle_fuel_types DROP CONSTRAINT vehicle_fuel_types_pkey;
    ALTER TABLE vehicle_fuel_types ADD COLUMN id BIGSERIAL PRIMARY KEY;
    ALTER TABLE vehicle_fuel_types ADD CONSTRAINT uq_vehicle_fuel_types_code UNIQUE (code);
  END IF;

  IF to_regclass('public.vehicle_transmission_types') IS NOT NULL
     AND NOT EXISTS (
       SELECT 1 FROM information_schema.columns
       WHERE table_schema = 'public' AND table_name = 'vehicle_transmission_types' AND column_name = 'id'
     ) THEN
    ALTER TABLE vehicle_transmission_types DROP CONSTRAINT vehicle_transmission_types_pkey;
    ALTER TABLE vehicle_transmission_types ADD COLUMN id BIGSERIAL PRIMARY KEY;
    ALTER TABLE vehicle_transmission_types ADD CONSTRAINT uq_vehicle_transmission_types_code UNIQUE (code);
  END IF;
END $migration$;
