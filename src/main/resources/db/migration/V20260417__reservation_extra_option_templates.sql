CREATE TABLE IF NOT EXISTS reservation_extra_option_templates (
    id CHAR(36) NOT NULL PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    code VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    icon VARCHAR(512),
    line_order INT NOT NULL DEFAULT 0,
    active BIT(1) NOT NULL DEFAULT 1,
    requires_co_driver_details BIT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_reservation_extra_option_templates_code (code)
);

INSERT INTO reservation_extra_option_templates (
    id, created_at, updated_at, code, title, description, price, icon, line_order, active, requires_co_driver_details
) VALUES
(
    'a1000001-0000-4000-8000-000000000001',
    NOW(6),
    NOW(6),
    'BABY_SEAT',
    'Bebek koltuğu',
    'Çocuk güvenliği için montajlı bebek koltuğu.',
    350.00,
    NULL,
    10,
    1,
    0
),
(
    'a1000001-0000-4000-8000-000000000002',
    NOW(6),
    NOW(6),
    'ADDITIONAL_DRIVER',
    'Ek şöför',
    'İkinci sürücü; ehliyet/pasaport numarası ve net görseller zorunludur.',
    350.00,
    NULL,
    20,
    1,
    1
),
(
    'a1000001-0000-4000-8000-000000000003',
    NOW(6),
    NOW(6),
    'GREEN_INSURANCE_ME',
    'Yeşil sigorta (Karadağ)',
    'Karadağ sınır geçişleri için yeşil sigorta.',
    700.00,
    NULL,
    30,
    1,
    0
),
(
    'a1000001-0000-4000-8000-000000000004',
    NOW(6),
    NOW(6),
    'GREEN_INSURANCE_BALKAN',
    'Yeşil sigorta (AL · XK · ME · MK)',
    'Arnavutluk, Kosova, Karadağ ve Kuzey Makedonya için geçerli yeşil sigorta.',
    1400.00,
    NULL,
    40,
    1,
    0
);
