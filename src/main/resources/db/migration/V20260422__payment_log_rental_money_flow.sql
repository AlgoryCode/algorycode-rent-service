DO $migration$
BEGIN
  IF to_regclass('public.payment_logs') IS NULL THEN
    RETURN;
  END IF;
  ALTER TABLE payment_logs ADD COLUMN IF NOT EXISTS rental_id BIGINT NULL;
  ALTER TABLE payment_logs ADD COLUMN IF NOT EXISTS money_flow VARCHAR(16) NOT NULL DEFAULT 'inbound';
  ALTER TABLE payment_logs ADD COLUMN IF NOT EXISTS rental_revenue_eur DECIMAL(14, 2) NULL;
  IF to_regclass('public.rentals') IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_payment_logs_rental'
  ) THEN
    ALTER TABLE payment_logs
      ADD CONSTRAINT fk_payment_logs_rental
      FOREIGN KEY (rental_id) REFERENCES rentals (id) ON DELETE SET NULL;
  END IF;
  CREATE INDEX IF NOT EXISTS idx_payment_logs_rental_id ON payment_logs (rental_id);
END $migration$;
