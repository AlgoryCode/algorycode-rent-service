ALTER TABLE rental_requests
  ADD COLUMN start_time TIME NULL AFTER end_date,
  ADD COLUMN return_time TIME NULL AFTER start_time;
