DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.columns c
                   WHERE c.table_schema = current_schema()
                     AND c.table_name = 'vehicles'
                     AND c.column_name = 'vehicle_status_id')
            AND NOT EXISTS (SELECT 1
                            FROM information_schema.columns c
                            WHERE c.table_schema = current_schema()
                              AND c.table_name = 'vehicles'
                              AND c.column_name = 'vehicle_status') THEN
            ALTER TABLE vehicles ADD COLUMN vehicle_status VARCHAR(32);
            UPDATE vehicles v
            SET vehicle_status = CASE lower(vs.code)
                                       WHEN 'available' THEN 'ACTIVE'
                                       WHEN 'rented' THEN 'RENTED'
                                       WHEN 'maintenance' THEN 'MAINTENANCE'
                                       WHEN 'reserved' THEN 'PENDING'
                                       ELSE 'ACTIVE'
                                   END
            FROM vehicle_statuses vs
            WHERE v.vehicle_status_id IS NOT NULL
              AND v.vehicle_status_id = vs.id;
            UPDATE vehicles SET vehicle_status = 'ACTIVE' WHERE vehicle_status IS NULL;
            ALTER TABLE vehicles ALTER COLUMN vehicle_status SET NOT NULL;
            ALTER TABLE vehicles DROP COLUMN IF EXISTS vehicle_status_id;
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.columns c
                   WHERE c.table_schema = current_schema()
                     AND c.table_name = 'rentals'
                     AND c.column_name = 'rental_status_id')
            AND NOT EXISTS (SELECT 1
                            FROM information_schema.columns c
                            WHERE c.table_schema = current_schema()
                              AND c.table_name = 'rentals'
                              AND c.column_name = 'rental_status') THEN
            ALTER TABLE rentals ADD COLUMN rental_status VARCHAR(32);
            UPDATE rentals r
            SET rental_status = CASE lower(trim(rs.code))
                                      WHEN 'active' THEN 'ACTIVE'
                                      WHEN 'pending' THEN 'PENDING'
                                      WHEN 'completed' THEN 'COMPLETED'
                                      WHEN 'cancelled' THEN 'CANCELLED'
                                      WHEN 'iptal' THEN 'CANCELLED'
                                      WHEN 'tamamlandi' THEN 'COMPLETED'
                                      WHEN 'tamamlandı' THEN 'COMPLETED'
                                      ELSE 'ACTIVE'
                                  END
            FROM rental_statuses rs
            WHERE r.rental_status_id IS NOT NULL
              AND r.rental_status_id = rs.id;
            UPDATE rentals SET rental_status = 'ACTIVE' WHERE rental_status IS NULL;
            ALTER TABLE rentals ALTER COLUMN rental_status SET NOT NULL;
            ALTER TABLE rentals DROP COLUMN IF EXISTS rental_status_id;
        END IF;
    END
$$;
