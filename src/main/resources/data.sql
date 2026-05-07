INSERT INTO vehicle_statuses (code, label_tr, sort_order)
SELECT v.code, v.label_tr, v.sort_order
FROM (VALUES ('available', 'Musait', 1),
             ('rented', 'Kirada', 2),
             ('maintenance', 'Bakimda', 3),
             ('reserved', 'Rezerve', 4)) AS v(code, label_tr, sort_order)
WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = 'vehicle_statuses')
  AND NOT EXISTS (SELECT 1 FROM vehicle_statuses s WHERE lower(s.code) = lower(v.code));

INSERT INTO rental_statuses (code, label_tr, sort_order)
SELECT v.code, v.label_tr, v.sort_order
FROM (VALUES ('active', 'Aktif', 1),
             ('completed', 'Tamamlandi', 2),
             ('cancelled', 'Iptal', 3)) AS v(code, label_tr, sort_order)
WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = 'rental_statuses')
  AND NOT EXISTS (SELECT 1 FROM rental_statuses s WHERE lower(s.code) = lower(v.code));

INSERT INTO vehicle_transmission_types (code, label_tr, sort_order)
SELECT v.code, v.label_tr, v.sort_order
FROM (VALUES ('automatic', 'Otomatik', 1),
             ('manual', 'Manuel', 2)) AS v(code, label_tr, sort_order)
WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = 'vehicle_transmission_types')
  AND NOT EXISTS (SELECT 1 FROM vehicle_transmission_types t WHERE lower(t.code) = lower(v.code));

INSERT INTO vehicle_fuel_types (code, label_tr, sort_order)
SELECT v.code, v.label_tr, v.sort_order
FROM (VALUES ('gasoline', 'Benzin', 1),
             ('diesel', 'Dizel', 2),
             ('hybrid', 'Hibrit', 3),
             ('electric', 'Elektrik', 4)) AS v(code, label_tr, sort_order)
WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = 'vehicle_fuel_types')
  AND NOT EXISTS (SELECT 1 FROM vehicle_fuel_types t WHERE lower(t.code) = lower(v.code));

INSERT INTO vehicle_body_styles (code, label_tr, sort_order)
SELECT v.code, v.label_tr, v.sort_order
FROM (VALUES ('sedan', 'Sedan', 1),
             ('suv', 'SUV', 2),
             ('hatchback', 'Hatchback', 3)) AS v(code, label_tr, sort_order)
WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = current_schema() AND table_name = 'vehicle_body_styles')
  AND NOT EXISTS (SELECT 1 FROM vehicle_body_styles t WHERE lower(t.code) = lower(v.code));
