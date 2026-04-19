ALTER TABLE payment_logs
  ADD COLUMN rental_id CHAR(36) NULL,
  ADD COLUMN money_flow VARCHAR(16) NOT NULL DEFAULT 'inbound',
  ADD COLUMN rental_revenue_eur DECIMAL(14, 2) NULL,
  ADD CONSTRAINT fk_payment_logs_rental FOREIGN KEY (rental_id) REFERENCES rentals (id) ON DELETE SET NULL;

CREATE INDEX idx_payment_logs_rental_id ON payment_logs (rental_id);
