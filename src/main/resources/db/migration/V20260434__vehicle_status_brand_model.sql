CREATE TABLE vehicle_statuses (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(32) NOT NULL,
  label_tr VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL
);

CREATE UNIQUE INDEX uq_vehicle_statuses_code ON vehicle_statuses (lower(trim(code)));

INSERT INTO vehicle_statuses (code, label_tr, sort_order) VALUES
('available', 'Müsait', 1),
('maintenance', 'Bakımda', 2),
('rented', 'Kirada', 3);

CREATE TABLE vehicle_brands (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_vehicle_brands_name_lower ON vehicle_brands (lower(trim(name)));

CREATE TABLE vehicle_models (
  id BIGSERIAL PRIMARY KEY,
  brand_id BIGINT NOT NULL REFERENCES vehicle_brands (id) ON DELETE RESTRICT,
  name VARCHAR(255) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_vehicle_models_brand_name_lower ON vehicle_models (brand_id, lower(trim(name)));

ALTER TABLE vehicles ADD COLUMN vehicle_status_id BIGINT;
UPDATE vehicles v
SET vehicle_status_id = s.id
FROM vehicle_statuses s
WHERE v.status = s.code;

UPDATE vehicles
SET vehicle_status_id = (SELECT id FROM vehicle_statuses WHERE code = 'available' LIMIT 1)
WHERE vehicle_status_id IS NULL;

ALTER TABLE vehicles ALTER COLUMN vehicle_status_id SET NOT NULL;
ALTER TABLE vehicles
  ADD CONSTRAINT fk_vehicles_vehicle_status
  FOREIGN KEY (vehicle_status_id) REFERENCES vehicle_statuses (id);

ALTER TABLE vehicles DROP COLUMN status;

INSERT INTO vehicle_brands (name, sort_order)
SELECT DISTINCT trim(t.name), 0
FROM (
  SELECT coalesce(nullif(trim(brand), ''), 'Genel') AS name FROM vehicles
) t
WHERE NOT EXISTS (
  SELECT 1 FROM vehicle_brands b WHERE lower(trim(b.name)) = lower(trim(t.name))
);

INSERT INTO vehicle_brands (name, sort_order)
SELECT 'Genel', 0
WHERE NOT EXISTS (SELECT 1 FROM vehicle_brands);

INSERT INTO vehicle_models (brand_id, name, sort_order)
SELECT b.id, trim(t.mn), 0
FROM (
  SELECT coalesce(nullif(trim(v.brand), ''), 'Genel') AS bn,
         coalesce(nullif(trim(v.model), ''), '—') AS mn
  FROM vehicles v
  GROUP BY coalesce(nullif(trim(v.brand), ''), 'Genel'),
           coalesce(nullif(trim(v.model), ''), '—')
) t
JOIN vehicle_brands b ON lower(trim(b.name)) = lower(trim(t.bn))
WHERE NOT EXISTS (
  SELECT 1 FROM vehicle_models m
  WHERE m.brand_id = b.id AND lower(trim(m.name)) = lower(trim(t.mn))
);

INSERT INTO vehicle_models (brand_id, name, sort_order)
SELECT (SELECT MIN(id) FROM vehicle_brands), '—', 0
WHERE NOT EXISTS (SELECT 1 FROM vehicle_models);

ALTER TABLE vehicles ADD COLUMN vehicle_model_id BIGINT;

UPDATE vehicles v
SET vehicle_model_id = m.id
FROM vehicle_models m
JOIN vehicle_brands b ON m.brand_id = b.id
WHERE lower(trim(b.name)) = lower(trim(coalesce(nullif(trim(v.brand), ''), 'Genel')))
  AND lower(trim(m.name)) = lower(trim(coalesce(nullif(trim(v.model), ''), '—')));

UPDATE vehicles
SET vehicle_model_id = (SELECT id FROM vehicle_models ORDER BY id LIMIT 1)
WHERE vehicle_model_id IS NULL;

ALTER TABLE vehicles ALTER COLUMN vehicle_model_id SET NOT NULL;
ALTER TABLE vehicles
  ADD CONSTRAINT fk_vehicles_vehicle_model
  FOREIGN KEY (vehicle_model_id) REFERENCES vehicle_models (id);

ALTER TABLE vehicles DROP COLUMN brand;
ALTER TABLE vehicles DROP COLUMN model;
