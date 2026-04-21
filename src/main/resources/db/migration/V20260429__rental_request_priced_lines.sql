-- Kiralama talebi: gece sayısı özeti + faturalandırma kalemleri (TRY, handover EUR metadata).
ALTER TABLE rental_requests
  ADD COLUMN rental_nights INT NULL,
  ADD COLUMN pricing_total_try DECIMAL(12, 2) NULL;

CREATE TABLE rental_request_priced_lines (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  rental_request_id BIGINT NOT NULL,
  line_type VARCHAR(40) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT NULL,
  quantity INT NOT NULL DEFAULT 1,
  unit_amount DECIMAL(12, 2) NOT NULL,
  line_amount DECIMAL(12, 2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'TRY',
  line_order INT NOT NULL DEFAULT 0,
  priced_at DATETIME(6) NULL,
  source_reservation_extra_template_id BIGINT NULL,
  source_vehicle_option_definition_id BIGINT NULL,
  return_handover_location_id BIGINT NULL,
  metadata TEXT NULL,
  PRIMARY KEY (id),
  KEY idx_rrpl_rental_request (rental_request_id),
  CONSTRAINT fk_rrpl_rental_request FOREIGN KEY (rental_request_id) REFERENCES rental_requests (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
