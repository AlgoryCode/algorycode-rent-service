package com.algorycode.rent.api.dto;

import java.time.LocalDate;

/**
 * Araç takviminde boyanacak kapalı aralık. {@code startDate} ve {@code endDate} her ikisi de
 * <strong>dahil</strong> (gün bazlı kira modeli); UI tarafında son günü atlamayın.
 */
public record VehicleOccupancyRangeDto(
    Long id, VehicleOccupancySource source, LocalDate startDate, LocalDate endDate) {}
