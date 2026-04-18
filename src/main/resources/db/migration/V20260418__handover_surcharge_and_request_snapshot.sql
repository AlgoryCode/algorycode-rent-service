-- Alış/teslim noktası başına ek ücret (EUR) ve talep anındaki özet (EUR).
ALTER TABLE handover_locations
  ADD COLUMN surcharge_eur DECIMAL(10, 2) NOT NULL DEFAULT 0.00;

ALTER TABLE rental_requests
  ADD COLUMN handover_pickup_leg_eur DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
  ADD COLUMN handover_return_leg_eur DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
  ADD COLUMN handover_route_eur DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
  ADD COLUMN handover_total_eur DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
