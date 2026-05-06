INSERT INTO vehicle_brands (id, name, sort_order)
SELECT v.id, v.name, v.sort_order
FROM (VALUES (1, 'Toyota', 1),
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
             (20, 'Jaguar', 20)) AS v(id, name, sort_order)
WHERE NOT EXISTS (SELECT 1
                  FROM vehicle_brands e
                  WHERE e.id = v.id OR lower(e.name) = lower(v.name));

SELECT setval(pg_get_serial_sequence('vehicle_brands', 'id'),
              (SELECT COALESCE(MAX(id), 1) FROM vehicle_brands));
