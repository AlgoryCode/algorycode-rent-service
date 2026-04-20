-- Katalog birincil anahtar: UUID yerine otomatik artan BIGINT (IDENTITY).
ALTER TABLE vehicle_body_styles DROP PRIMARY KEY;
ALTER TABLE vehicle_body_styles DROP COLUMN id;
ALTER TABLE vehicle_body_styles ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE vehicle_fuel_types DROP PRIMARY KEY;
ALTER TABLE vehicle_fuel_types DROP COLUMN id;
ALTER TABLE vehicle_fuel_types ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE vehicle_transmission_types DROP PRIMARY KEY;
ALTER TABLE vehicle_transmission_types DROP COLUMN id;
ALTER TABLE vehicle_transmission_types ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;
