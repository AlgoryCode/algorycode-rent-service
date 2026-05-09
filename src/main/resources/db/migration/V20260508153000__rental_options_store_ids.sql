ALTER TABLE rental_options
    ADD COLUMN IF NOT EXISTS vehicle_option_definition_id BIGINT,
    ADD COLUMN IF NOT EXISTS reservation_extra_template_id BIGINT;

ALTER TABLE rental_options
    ALTER COLUMN title DROP NOT NULL,
    ALTER COLUMN price DROP NOT NULL;

DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_rental_options_vehicle_option_definition'
    ) THEN
        ALTER TABLE rental_options
            ADD CONSTRAINT fk_rental_options_vehicle_option_definition
                FOREIGN KEY (vehicle_option_definition_id)
                    REFERENCES vehicle_option_definitions (id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_rental_options_reservation_extra_template'
    ) THEN
        ALTER TABLE rental_options
            ADD CONSTRAINT fk_rental_options_reservation_extra_template
                FOREIGN KEY (reservation_extra_template_id)
                    REFERENCES reservation_extra_option_templates (id);
    END IF;
END
$$;
