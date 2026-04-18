-- Araç öne çıkanlar (acente tarafından opsiyonel; sıra line_order ile)

CREATE TABLE IF NOT EXISTS vehicle_highlights (
  id CHAR(36) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  vehicle_id CHAR(36) NOT NULL,
  line_order INT NOT NULL,
  text VARCHAR(500) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_vehicle_highlights_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id) ON DELETE CASCADE
);

CREATE INDEX idx_vehicle_highlights_vehicle_id ON vehicle_highlights (vehicle_id);
