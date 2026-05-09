UPDATE vehicles
SET vehicle_status = upper(trim(vehicle_status))
WHERE vehicle_status IS NOT NULL;
