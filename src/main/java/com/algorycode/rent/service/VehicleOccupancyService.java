package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.VehicleCalendarOccupancyDto;
import com.algorycode.rent.api.dto.VehicleOccupancyRangeDto;
import com.algorycode.rent.api.dto.VehicleOccupancySource;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.request.RentalRequestStatus;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.repository.RentalRequestRepository;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.service.support.DateRangeValidator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Takvim UI için birleşik doluluk: yalnızca aktif statülü kiralamalar ile onaylı/bekleyen
 * {@code RentalRequest} aralıkları. Tarihler gün bazlı ve uçlar dahildir.
 */
@Service
public class VehicleOccupancyService {

  private static final List<RentalRequestStatus> BLOCKING_REQUEST_STATUSES =
      List.of(RentalRequestStatus.pending, RentalRequestStatus.approved);

  private final VehicleRepository vehicleRepository;
  private final RentalRepository rentalRepository;
  private final RentalRequestRepository rentalRequestRepository;

  public VehicleOccupancyService(
      VehicleRepository vehicleRepository,
      RentalRepository rentalRepository,
      RentalRequestRepository rentalRequestRepository) {
    this.vehicleRepository = vehicleRepository;
    this.rentalRepository = rentalRepository;
    this.rentalRequestRepository = rentalRequestRepository;
  }

  @Transactional(readOnly = true)
  public VehicleCalendarOccupancyDto occupancy(Long vehicleId, LocalDate from, LocalDate to) {
    DateRangeValidator.requireEndNotBeforeStart(from, to);
    vehicleRepository
        .findByIdAndDeletedFalse(vehicleId)
        .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

    List<VehicleOccupancyRangeDto> ranges = new ArrayList<>();
    for (var r : rentalRepository.findCalendarBlockingRentals(vehicleId, from, to)) {
      ranges.add(
          new VehicleOccupancyRangeDto(
              r.getId(), VehicleOccupancySource.rental, r.getStartDate(), r.getEndDate()));
    }
    for (var rr :
        rentalRequestRepository.findBlockingForVehicleCalendar(
            vehicleId, from, to, BLOCKING_REQUEST_STATUSES)) {
      ranges.add(
          new VehicleOccupancyRangeDto(
              rr.getId(),
              VehicleOccupancySource.rental_request,
              rr.getStartDate(),
              rr.getEndDate()));
    }
    ranges.sort(
        Comparator.comparing(VehicleOccupancyRangeDto::startDate)
            .thenComparing(VehicleOccupancyRangeDto::endDate)
            .thenComparing(r -> r.source().name()));
    return new VehicleCalendarOccupancyDto(from, to, List.copyOf(ranges));
  }
}
