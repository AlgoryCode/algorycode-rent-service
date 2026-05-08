CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    national_id VARCHAR(32) NOT NULL,
    passport_no VARCHAR(32) NOT NULL,
    phone VARCHAR(32) NOT NULL,
    email VARCHAR(255),
    birth_date DATE,
    driver_license_no VARCHAR(64),
    driver_license_image_data_url TEXT,
    passport_image_data_url TEXT
);

ALTER TABLE rentals ADD COLUMN IF NOT EXISTS customer_id BIGINT;

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_schema = current_schema()
                     AND table_name = 'rentals'
                     AND column_name = 'full_name') THEN
            INSERT INTO customers (created_at, updated_at, full_name, national_id, passport_no, phone, email,
                                   birth_date, driver_license_no, driver_license_image_data_url,
                                   passport_image_data_url)
            SELECT DISTINCT ON (
                CASE
                    WHEN NULLIF(TRIM(BOTH FROM COALESCE(r.national_id, '')), '') IS NOT NULL
                        THEN 'tc:' || UPPER(TRIM(BOTH FROM r.national_id))
                    ELSE 'ph:' || TRIM(BOTH FROM COALESCE(r.phone, '')) END)
                r.created_at,
                r.updated_at,
                r.full_name,
                COALESCE(NULLIF(TRIM(BOTH FROM r.national_id), ''), ''),
                COALESCE(NULLIF(TRIM(BOTH FROM r.passport_no), ''), ''),
                TRIM(BOTH FROM r.phone),
                NULLIF(TRIM(BOTH FROM r.email), ''),
                r.birth_date,
                NULLIF(TRIM(BOTH FROM r.driver_license_no), ''),
                r.driver_license_image_data_url,
                r.passport_image_data_url
            FROM rentals r
            ORDER BY CASE
                         WHEN NULLIF(TRIM(BOTH FROM COALESCE(r.national_id, '')), '') IS NOT NULL
                             THEN 'tc:' || UPPER(TRIM(BOTH FROM r.national_id))
                         ELSE 'ph:' || TRIM(BOTH FROM COALESCE(r.phone, '')) END,
                     r.id;

            UPDATE rentals r
            SET customer_id = c.id
            FROM customers c
            WHERE r.customer_id IS NULL
              AND ((NULLIF(TRIM(BOTH FROM COALESCE(r.national_id, '')), '') IS NOT NULL
                AND UPPER(TRIM(BOTH FROM r.national_id)) = UPPER(TRIM(BOTH FROM c.national_id)))
                OR (NULLIF(TRIM(BOTH FROM COALESCE(r.national_id, '')), '') IS NULL
                    AND TRIM(BOTH FROM COALESCE(r.phone, '')) = TRIM(BOTH FROM c.phone)
                    AND TRIM(BOTH FROM COALESCE(c.national_id, '')) = ''));

            ALTER TABLE rentals DROP COLUMN IF EXISTS full_name;
            ALTER TABLE rentals DROP COLUMN IF EXISTS national_id;
            ALTER TABLE rentals DROP COLUMN IF EXISTS passport_no;
            ALTER TABLE rentals DROP COLUMN IF EXISTS phone;
            ALTER TABLE rentals DROP COLUMN IF EXISTS email;
            ALTER TABLE rentals DROP COLUMN IF EXISTS birth_date;
            ALTER TABLE rentals DROP COLUMN IF EXISTS driver_license_no;
            ALTER TABLE rentals DROP COLUMN IF EXISTS driver_license_image_data_url;
            ALTER TABLE rentals DROP COLUMN IF EXISTS passport_image_data_url;
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM pg_constraint
                       WHERE conname = 'fk_rentals_customer') THEN
            IF (SELECT COUNT(*) FROM rentals WHERE customer_id IS NULL) = 0 THEN
                ALTER TABLE rentals ALTER COLUMN customer_id SET NOT NULL;
                ALTER TABLE rentals
                    ADD CONSTRAINT fk_rentals_customer FOREIGN KEY (customer_id) REFERENCES customers (id);
            END IF;
        END IF;
    END
$$;
