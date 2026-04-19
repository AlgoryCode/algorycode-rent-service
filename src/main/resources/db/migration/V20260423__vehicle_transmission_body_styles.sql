-- Araç gövde tipleri (panel / kullanıcı araması için referans)
CREATE TABLE IF NOT EXISTS vehicle_body_styles (
  code VARCHAR(32) NOT NULL PRIMARY KEY,
  label_tr VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0
);

INSERT INTO vehicle_body_styles (code, label_tr, sort_order) VALUES
  ('SEDAN', 'Sedan', 10),
  ('HATCHBACK', 'Hatchback', 20),
  ('SUV', 'SUV / Jeep', 30),
  ('COUPE', 'Coupe', 40),
  ('ESTATE', 'Station wagon', 50),
  ('VAN', 'Van / Panelvan', 60),
  ('PICKUP', 'Pickup', 70),
  ('MINIBUS', 'Minibüs', 80)
ON DUPLICATE KEY UPDATE label_tr = VALUES(label_tr), sort_order = VALUES(sort_order);

ALTER TABLE vehicles
  ADD COLUMN IF NOT EXISTS transmission_type VARCHAR(32) NULL AFTER luggage,
  ADD COLUMN IF NOT EXISTS body_style_code VARCHAR(32) NULL AFTER transmission_type;
