ALTER TABLE vehicles
    ADD COLUMN IF NOT EXISTS fuel_type_id BIGINT REFERENCES vehicle_fuel_types (id);

UPDATE vehicles v
SET fuel_type_id = f.id
FROM vehicle_fuel_types f
WHERE v.fuel_type IS NOT NULL
  AND v.fuel_type_id IS NULL
  AND lower(trim(v.fuel_type)) = lower(trim(f.code));

ALTER TABLE vehicles DROP COLUMN IF EXISTS fuel_type;
