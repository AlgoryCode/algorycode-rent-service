CREATE TABLE discount_coupons (
  id             BIGSERIAL PRIMARY KEY,
  code           VARCHAR(64)    NOT NULL,
  discount_type  VARCHAR(16)    NOT NULL,
  discount_value NUMERIC(12,2)  NOT NULL,
  description    VARCHAR(255),
  active         BOOLEAN        NOT NULL DEFAULT TRUE,
  usage_limit    INT,
  usage_count    INT            NOT NULL DEFAULT 0,
  expires_at     TIMESTAMP,
  created_at     TIMESTAMP      NOT NULL,
  updated_at     TIMESTAMP      NOT NULL
);
CREATE UNIQUE INDEX uq_discount_coupons_code ON discount_coupons (lower(trim(code)));
