ALTER TABLE vehicles
  ADD COLUMN fuel_type VARCHAR(64) NULL AFTER engine,
  ADD COLUMN body_color VARCHAR(64) NULL AFTER fuel_type;
