-- Araç başına birden fazla teslim (RETURN) handover noktası; tek FK yerine ilişki tablosu.
-- Hibernate ddl-auto=update ortamlarında tablo oluşabilir; bu betik elle / prod için.
-- PK ve FK türleri uygulama modeliyle uyumlu BIGINT (IDENTITY).

CREATE TABLE IF NOT EXISTS vehicle_allowed_return_handovers (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  vehicle_id BIGINT NOT NULL,
  handover_location_id BIGINT NOT NULL,
  line_order INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT fk_varh_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id) ON DELETE CASCADE,
  CONSTRAINT fk_varh_handover FOREIGN KEY (handover_location_id) REFERENCES handover_locations (id),
  CONSTRAINT uk_vehicle_allowed_return_vehicle_handover UNIQUE (vehicle_id, handover_location_id)
);

CREATE INDEX idx_varh_vehicle_id ON vehicle_allowed_return_handovers (vehicle_id);

INSERT INTO vehicle_allowed_return_handovers (created_at, updated_at, vehicle_id, handover_location_id, line_order)
SELECT NOW(6), NOW(6), v.id, v.default_return_handover_location_id, 0
FROM vehicles v
WHERE v.default_return_handover_location_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM vehicle_allowed_return_handovers x WHERE x.vehicle_id = v.id);

-- Eski `vehicles.default_return_handover_location_id` sütunu JPA modelinden kaldırıldı; DB’de kalabilir (hibernate ddl-update genelde sütun silmez).
