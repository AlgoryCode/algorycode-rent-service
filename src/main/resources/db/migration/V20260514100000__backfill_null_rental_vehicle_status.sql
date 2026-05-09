DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.columns c
                   WHERE c.table_schema = current_schema()
                     AND c.table_name = 'rentals'
                     AND c.column_name = 'rental_status') THEN
            UPDATE rentals SET rental_status = 'ACTIVE' WHERE rental_status IS NULL;
            ALTER TABLE rentals ALTER COLUMN rental_status SET NOT NULL;
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.columns c
                   WHERE c.table_schema = current_schema()
                     AND c.table_name = 'vehicles'
                     AND c.column_name = 'vehicle_status') THEN
            UPDATE vehicles SET vehicle_status = 'ACTIVE' WHERE vehicle_status IS NULL;
            ALTER TABLE vehicles ALTER COLUMN vehicle_status SET NOT NULL;
        END IF;
    END
$$;
