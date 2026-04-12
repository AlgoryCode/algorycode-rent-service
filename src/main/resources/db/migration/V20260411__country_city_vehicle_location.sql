-- Country / City / Vehicle location migration
-- NOTE: Project currently uses hibernate.ddl-auto=update.
-- This SQL is provided for controlled/manual migration environments.

CREATE TABLE IF NOT EXISTS cities (
  id CHAR(36) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  name VARCHAR(128) NOT NULL,
  country_id CHAR(36) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_cities_country FOREIGN KEY (country_id) REFERENCES countries (id)
);

CREATE INDEX idx_cities_country_id ON cities (country_id);
CREATE INDEX idx_cities_name ON cities (name);

ALTER TABLE vehicles
  ADD COLUMN IF NOT EXISTS city_id CHAR(36) NULL,
  ADD COLUMN IF NOT EXISTS engine VARCHAR(255) NULL,
  ADD COLUMN IF NOT EXISTS seats INT NULL,
  ADD COLUMN IF NOT EXISTS luggage INT NULL;

ALTER TABLE vehicles
  ADD CONSTRAINT fk_vehicles_city FOREIGN KEY (city_id) REFERENCES cities (id);

CREATE INDEX idx_vehicles_city_id ON vehicles (city_id);

-- Optional backfill: map vehicles.country_code -> first city of that country if present.
-- UPDATE vehicles v
-- JOIN countries c ON c.code = v.country_code
-- JOIN cities ci ON ci.country_id = c.id
-- SET v.city_id = ci.id
-- WHERE v.city_id IS NULL;
