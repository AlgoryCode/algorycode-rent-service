INSERT INTO vehicle_brands (id, name, sort_order)
VALUES (1, 'Toyota', 1),
       (2, 'Volkswagen', 2),
       (3, 'Hyundai', 3),
       (4, 'Ford', 4),
       (5, 'BYD', 5),
       (6, 'Honda', 6),
       (7, 'Nissan', 7),
       (8, 'Suzuki', 8),
       (9, 'BMW', 9),
       (10, 'Mercedes-Benz', 10),
       (11, 'Audi', 11),
       (12, 'Škoda', 12),
       (13, 'Renault', 13),
       (14, 'Peugeot', 14),
       (15, 'Fiat', 15),
       (16, 'Tesla', 16),
       (17, 'Mazda', 17),
       (18, 'Kia', 18),
       (19, 'Volvo', 19),
       (20, 'Jaguar', 20)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    sort_order = EXCLUDED.sort_order;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.sequences
                   WHERE sequence_schema = current_schema()
                     AND sequence_name = 'vehicle_brands_id_seq') THEN
            PERFORM setval(pg_get_serial_sequence('vehicle_brands', 'id'),
                           (SELECT COALESCE(MAX(id), 1) FROM vehicle_brands));
        END IF;
    END
$$;
