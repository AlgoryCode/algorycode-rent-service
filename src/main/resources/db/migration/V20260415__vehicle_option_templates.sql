CREATE TABLE IF NOT EXISTS vehicle_option_templates (
    id CHAR(36) NOT NULL PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    icon VARCHAR(512),
    line_order INT NOT NULL DEFAULT 0,
    active BIT(1) NOT NULL DEFAULT 1
);
