package com.algorycode.rent.api.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Araç takviminde boyanacak kapalı aralık. {@code startDate} ve {@code endDate} her ikisi de
 * <strong>dahil</strong> (gün bazlı kira modeli); UI tarafında son günü atlamayın.
 */
public record VehicleOccupancyRangeDto(
    UUID id, VehicleOccupancySource source, LocalDate startDate, LocalDate endDate) {}
