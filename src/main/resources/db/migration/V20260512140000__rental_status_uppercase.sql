UPDATE rentals
SET rental_status = upper(trim(rental_status))
WHERE rental_status IS NOT NULL;
