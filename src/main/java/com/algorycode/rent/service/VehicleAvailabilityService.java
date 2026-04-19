package com.algorycode.rent.service;

import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.domain.request.RentalRequestStatus;
import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.repository.RentalRequestRepository;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.service.support.DateRangeValidator;
import com.algorycode.rent.service.support.RentalAvailabilityRules;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Filo listesinde tarih + handover uygunluk süzmesi (VehicleService orchestration dışı). İptal olmayan
 * {@link Rental} ile birlikte pending/approved {@link com.algorycode.rent.domain.request.RentalRequest}
 * kayıtları aynı çakışma + tampon günü kurallarıyla değerlendirilir.
 */
@Service
public class VehicleAvailabilityService {

  private static final List<RentalRequestStatus> BLOCKING_REQUEST_STATUSES =
      List.of(RentalRequestStatus.pending, RentalRequestStatus.approved);

  private final VehicleRepository vehicleRepository;
  private final RentalRepository rentalRepository;
  private final RentalRequestRepository rentalRequestRepository;

  public VehicleAvailabilityService(
      VehicleRepository vehicleRepository,
      RentalRepository rentalRepository,
      RentalRequestRepository rentalRequestRepository) {
    this.vehicleRepository = vehicleRepository;
    this.rentalRepository = rentalRepository;
    this.rentalRequestRepository = rentalRequestRepository;
  }

  @Transactional(readOnly = true)
  public List<Vehicle> listVehiclesMatchingAvailability(
      LocalDate availableFrom,
      LocalDate availableTo,
      UUID pickupHandoverLocationId,
      UUID returnHandoverLocationId,
      boolean includePartialAvailability) {
    if (availableFrom == null || availableTo == null) {
      throw new BadRequestException(
          "Uygunluk için availableFrom ve availableTo birlikte gönderilmelidir.");
    }
    DateRangeValidator.requireEndNotBeforeStart(availableFrom, availableTo);
    LocalDate windowEnd =
        availableTo.equals(LocalDate.MAX) ? availableTo : availableTo.plusDays(1);
    List<Rental> rentalCandidates =
        rentalRepository.findPotentiallyBlockingForAvailability(availableFrom, windowEnd);
    List<RentalRequest> requestCandidates =
        rentalRequestRepository.findPotentiallyBlockingRequestsForAvailability(
            availableFrom, windowEnd, BLOCKING_REQUEST_STATUSES);

    return vehicleRepository.findAllByDeletedFalse().stream()
        .filter(v -> !v.isMaintenance())
        .filter(v -> matchesHandoverFilters(v, pickupHandoverLocationId, returnHandoverLocationId))
        .filter(
            v ->
                passesAvailabilityForListing(
                    v.getId(),
                    availableFrom,
                    availableTo,
                    rentalCandidates,
                    requestCandidates,
                    includePartialAvailability))
        .toList();
  }

  private static boolean passesAvailabilityForListing(
      UUID vehicleId,
      LocalDate availableFrom,
      LocalDate availableTo,
      List<Rental> rentalCandidates,
      List<RentalRequest> requestCandidates,
      boolean includePartialAvailability) {
    boolean strict =
        isVehicleAvailableForRange(
            vehicleId, availableFrom, availableTo, rentalCandidates, requestCandidates);
    if (!includePartialAvailability) {
      return strict;
    }
    if (strict) {
      return true;
    }
    if (availableTo.isBefore(availableFrom.plusDays(1))) {
      return false;
    }
    LocalDate partialThrough = availableFrom.plusDays(1);
    return isVehicleAvailableForRange(
        vehicleId, availableFrom, partialThrough, rentalCandidates, requestCandidates);
  }

  private static boolean matchesHandoverFilters(
      Vehicle v, UUID pickupHandoverLocationId, UUID returnHandoverLocationId) {
    if (pickupHandoverLocationId != null) {
      if (v.getDefaultPickupHandoverLocation() == null) {
        return false;
      }
      if (!pickupHandoverLocationId.equals(v.getDefaultPickupHandoverLocation().getId())) {
        return false;
      }
    }
    if (returnHandoverLocationId != null) {
      List<UUID> allowedReturns = v.orderedReturnHandoverLocationIds();
      if (allowedReturns.isEmpty()) {
        return true;
      }
      return allowedReturns.contains(returnHandoverLocationId);
    }
    return true;
  }

  private static boolean isVehicleAvailableForRange(
      UUID vehicleId,
      LocalDate from,
      LocalDate to,
      List<Rental> rentalCandidates,
      List<RentalRequest> requestCandidates) {
    for (Rental r : rentalCandidates) {
      if (!r.getVehicle().getId().equals(vehicleId)) {
        continue;
      }
      if (RentalAvailabilityRules.rentalBlocksRequestedRange(
          from, to, r.getStartDate(), r.getEndDate())) {
        return false;
      }
    }
    for (RentalRequest rr : requestCandidates) {
      if (rr.getVehicle() == null || !rr.getVehicle().getId().equals(vehicleId)) {
        continue;
      }
      if (RentalAvailabilityRules.rentalBlocksRequestedRange(
          from, to, rr.getStartDate(), rr.getEndDate())) {
        return false;
      }
    }
    return true;
  }
}
