ALTER TABLE rentals ADD COLUMN IF NOT EXISTS birth_date DATE;

UPDATE rentals r
SET birth_date = c.birth_date
FROM customers c
WHERE r.customer_id IS NOT NULL
  AND c.id = r.customer_id
  AND r.birth_date IS NULL;
