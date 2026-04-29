package com.algorycode.rent.api.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Bir araç için pencere içi dolu aralıklar: yalnızca aktif statülü {@link VehicleOccupancySource#rental}
 * ile reddedilmemiş talepler ({@link VehicleOccupancySource#rental_request}). Tarihler uçtan uca
 * dahildir.
 */
public record VehicleCalendarOccupancyDto(
    LocalDate from, LocalDate to, List<VehicleOccupancyRangeDto> ranges) {}
