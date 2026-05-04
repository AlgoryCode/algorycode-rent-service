CREATE TABLE IF NOT EXISTS rental_statuses (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(32) NOT NULL,
  label_tr VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_rental_statuses_code ON rental_statuses (lower(trim(code)));

INSERT INTO rental_statuses (code, label_tr, sort_order)
SELECT t.code, t.label_tr, t.sort_order
FROM (
  VALUES
    ('active', 'Aktif', 1),
    ('pending', 'Beklemede', 2),
    ('completed', 'Tamamlandı', 3),
    ('cancelled', 'İptal', 4)
) AS t(code, label_tr, sort_order)
WHERE NOT EXISTS (
  SELECT 1
  FROM rental_statuses s
  WHERE lower(trim(s.code)) = lower(trim(t.code))
);

DO $migration$
BEGIN
  IF to_regclass('public.rentals') IS NULL THEN
    RETURN;
  END IF;

  ALTER TABLE rentals ADD COLUMN IF NOT EXISTS rental_status_id BIGINT;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'rentals' AND column_name = 'status'
  ) THEN
    UPDATE rentals r
    SET rental_status_id = s.id
    FROM rental_statuses s
    WHERE trim(lower(coalesce(r.status::text, ''))) = trim(lower(coalesce(s.code, '')))
      AND r.rental_status_id IS NULL;
  END IF;

  UPDATE rentals
  SET rental_status_id = (SELECT id FROM rental_statuses WHERE lower(trim(code)) = 'active' LIMIT 1)
  WHERE rental_status_id IS NULL;

  ALTER TABLE rentals ALTER COLUMN rental_status_id SET NOT NULL;

  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_rentals_rental_status') THEN
    ALTER TABLE rentals
      ADD CONSTRAINT fk_rentals_rental_status
      FOREIGN KEY (rental_status_id) REFERENCES rental_statuses (id);
  END IF;

  ALTER TABLE rentals DROP COLUMN IF EXISTS status;
END $migration$;
