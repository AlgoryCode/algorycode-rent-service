-- Optional user relation on reservations
-- user_id is nullable by design.

ALTER TABLE rentals
  ADD COLUMN IF NOT EXISTS user_id CHAR(36) NULL;

ALTER TABLE rental_requests
  ADD COLUMN IF NOT EXISTS user_id CHAR(36) NULL;

CREATE INDEX idx_rentals_user_id ON rentals (user_id);
CREATE INDEX idx_rental_requests_user_id ON rental_requests (user_id);
