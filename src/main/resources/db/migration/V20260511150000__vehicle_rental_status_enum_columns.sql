ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS vehicle_status VARCHAR(32);

UPDATE vehicles v
SET vehicle_status = CASE lower(vs.code)
                         WHEN 'available' THEN 'active'
                         WHEN 'rented' THEN 'rented'
                         WHEN 'maintenance' THEN 'maintenance'
                         WHEN 'reserved' THEN 'pending'
                         ELSE 'active'
                     END
FROM vehicle_statuses vs
WHERE v.vehicle_status_id IS NOT NULL
  AND v.vehicle_status_id = vs.id;

UPDATE vehicles SET vehicle_status = 'active' WHERE vehicle_status IS NULL;

ALTER TABLE vehicles ALTER COLUMN vehicle_status SET NOT NULL;

ALTER TABLE vehicles DROP COLUMN IF EXISTS vehicle_status_id;

ALTER TABLE rentals ADD COLUMN IF NOT EXISTS rental_status VARCHAR(32);

UPDATE rentals r
SET rental_status = CASE lower(trim(rs.code))
                        WHEN 'active' THEN 'active'
                        WHEN 'pending' THEN 'pending'
                        WHEN 'completed' THEN 'completed'
                        WHEN 'cancelled' THEN 'cancelled'
                        WHEN 'iptal' THEN 'cancelled'
                        WHEN 'tamamlandi' THEN 'completed'
                        WHEN 'tamamlandı' THEN 'completed'
                        ELSE 'active'
                    END
FROM rental_statuses rs
WHERE r.rental_status_id IS NOT NULL
  AND r.rental_status_id = rs.id;

UPDATE rentals SET rental_status = 'active' WHERE rental_status IS NULL;

ALTER TABLE rentals ALTER COLUMN rental_status SET NOT NULL;

ALTER TABLE rentals DROP COLUMN IF EXISTS rental_status_id;
