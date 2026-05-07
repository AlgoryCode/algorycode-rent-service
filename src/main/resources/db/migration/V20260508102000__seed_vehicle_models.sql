INSERT INTO vehicle_models (id, brand_id, name, sort_order)
VALUES (1, 1, 'Corolla', 1),
       (2, 1, 'Camry', 2),
       (3, 1, 'RAV4', 3),
       (4, 1, 'Yaris', 4),
       (5, 2, 'Golf', 1),
       (6, 2, 'Passat', 2),
       (7, 2, 'Tiguan', 3),
       (8, 3, 'i20', 1),
       (9, 3, 'Tucson', 2),
       (10, 3, 'Ioniq 5', 3),
       (11, 4, 'Focus', 1),
       (12, 4, 'Puma', 2),
       (13, 4, 'Kuga', 3),
       (14, 5, 'Atto 3', 1),
       (15, 5, 'Seal', 2),
       (16, 6, 'Civic', 1),
       (17, 6, 'CR-V', 2),
       (18, 7, 'Qashqai', 1),
       (19, 7, 'X-Trail', 2),
       (20, 8, 'Swift', 1),
       (21, 8, 'Vitara', 2),
       (22, 9, '3 Serisi', 1),
       (23, 9, 'X5', 2),
       (24, 10, 'C Serisi', 1),
       (25, 10, 'GLE', 2),
       (26, 11, 'A4', 1),
       (27, 11, 'Q5', 2),
       (28, 12, 'Octavia', 1),
       (29, 12, 'Kodiaq', 2),
       (30, 13, 'Clio', 1),
       (31, 13, 'Mégane', 2),
       (32, 14, '208', 1),
       (33, 14, '3008', 2),
       (34, 15, '500', 1),
       (35, 15, 'Egea', 2),
       (36, 16, 'Model 3', 1),
       (37, 16, 'Model Y', 2),
       (38, 17, 'Mazda3', 1),
       (39, 17, 'CX-5', 2),
       (40, 18, 'Ceed', 1),
       (41, 18, 'Sportage', 2),
       (42, 19, 'XC60', 1),
       (43, 19, 'XC90', 2),
       (44, 20, 'F-Pace', 1),
       (45, 20, 'XE', 2)
ON CONFLICT (id) DO UPDATE
SET brand_id = EXCLUDED.brand_id,
    name = EXCLUDED.name,
    sort_order = EXCLUDED.sort_order;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.sequences
                   WHERE sequence_schema = current_schema()
                     AND sequence_name = 'vehicle_models_id_seq') THEN
            PERFORM setval(pg_get_serial_sequence('vehicle_models', 'id'),
                           (SELECT COALESCE(MAX(id), 1) FROM vehicle_models));
        END IF;
    END
$$;
