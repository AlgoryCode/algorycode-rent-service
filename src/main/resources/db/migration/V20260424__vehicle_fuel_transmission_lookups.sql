-- Yakıt ve vites referansları (panel + API CRUD; uygulama açılışında seed güncellenir)
CREATE TABLE IF NOT EXISTS vehicle_fuel_types (
  code VARCHAR(32) NOT NULL PRIMARY KEY,
  label_tr VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS vehicle_transmission_types (
  code VARCHAR(32) NOT NULL PRIMARY KEY,
  label_tr VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0
);

INSERT INTO vehicle_fuel_types (code, label_tr, sort_order) VALUES
  ('benzin', 'Benzin', 10),
  ('dizel', 'Dizel', 20),
  ('hibrit', 'Hibrit', 30),
  ('elektrik', 'Elektrik', 40)
ON DUPLICATE KEY UPDATE label_tr = VALUES(label_tr), sort_order = VALUES(sort_order);

INSERT INTO vehicle_transmission_types (code, label_tr, sort_order) VALUES
  ('otomatik', 'Otomatik', 10),
  ('manuel', 'Manuel', 20)
ON DUPLICATE KEY UPDATE label_tr = VALUES(label_tr), sort_order = VALUES(sort_order);
