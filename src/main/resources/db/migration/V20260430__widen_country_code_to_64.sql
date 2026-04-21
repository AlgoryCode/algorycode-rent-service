-- Ülke kodu: kısa ISO veya uzun dahili kodlar (FE ile uyumlu üst sınır 64).
ALTER TABLE countries MODIFY COLUMN code VARCHAR(64) NOT NULL;
ALTER TABLE vehicles MODIFY COLUMN country_code VARCHAR(64) NULL;
