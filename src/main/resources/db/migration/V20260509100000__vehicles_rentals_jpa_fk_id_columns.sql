ALTER TABLE vehicles
    ADD COLUMN IF NOT EXISTS fuel_type_id BIGINT;

ALTER TABLE vehicles
    ADD COLUMN IF NOT EXISTS transmission_type_id BIGINT;

ALTER TABLE vehicles
    ADD COLUMN IF NOT EXISTS body_style_id BIGINT;

ALTER TABLE vehicles
    ADD COLUMN IF NOT EXISTS vehicle_status_id BIGINT;

ALTER TABLE vehicles
    ADD COLUMN IF NOT EXISTS vehicle_model_id BIGINT;

ALTER TABLE rentals
    ADD COLUMN IF NOT EXISTS rental_status_id BIGINT;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = current_schema()
                     AND table_name = 'vehicle_fuel_types')
            AND EXISTS (SELECT 1
                        FROM information_schema.columns
                        WHERE table_schema = current_schema()
                          AND table_name = 'vehicles'
                          AND column_name = 'fuel_type') THEN
            UPDATE vehicles v
            SET fuel_type_id = f.id
            FROM vehicle_fuel_types f
            WHERE v.fuel_type IS NOT NULL
              AND v.fuel_type_id IS NULL
              AND lower(trim(v.fuel_type::text)) = lower(trim(f.code));
            ALTER TABLE vehicles
                DROP COLUMN IF EXISTS fuel_type;
        END IF;
    END
$$;
