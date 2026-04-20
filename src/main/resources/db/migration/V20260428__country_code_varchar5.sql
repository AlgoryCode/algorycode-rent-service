-- Ülke kodu 2–5 harf (ISO alpha-2 veya dahili kodlar); araç üzerindeki denormalize alan uyumu.
ALTER TABLE countries MODIFY COLUMN code VARCHAR(5) NOT NULL;
ALTER TABLE vehicles MODIFY COLUMN country_code VARCHAR(5) NULL;
