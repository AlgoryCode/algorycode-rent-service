package com.algorycode.rent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.algorycode.rent.api.dto.VehicleOccupancySource;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalStatus;
import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.domain.request.RentalRequestStatus;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.repository.RentalRequestRepository;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.service.support.RentalTestFixtures;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleOccupancyServiceTest {

  @Mock private VehicleRepository vehicleRepository;
  @Mock private RentalRepository rentalRepository;
  @Mock private RentalRequestRepository rentalRequestRepository;

  @InjectMocks private VehicleOccupancyService vehicleOccupancyService;

  @Test
  void occupancy_mergesRentalsAndRequests_inclusiveDates() {
    Long vid = 1L;
    Vehicle v = new Vehicle();
    v.setId(vid);
    when(vehicleRepository.findByIdAndDeletedFalse(vid)).thenReturn(Optional.of(v));

    Long rid = 1L;
    Rental rental = new Rental();
    rental.setId(rid);
    rental.setStartDate(LocalDate.of(2026, 4, 19));
    rental.setEndDate(LocalDate.of(2026, 4, 21));
    RentalTestFixtures.attachRentalStatus(rental, RentalStatus.active);
    when(rentalRepository.findCalendarBlockingRentals(eq(vid), any(), any()))
        .thenReturn(List.of(rental));

    Long qid = 1L;
    RentalRequest req = new RentalRequest();
    req.setId(qid);
    req.setStartDate(LocalDate.of(2026, 5, 1));
    req.setEndDate(LocalDate.of(2026, 5, 3));
    req.setStatus(RentalRequestStatus.pending);
    when(rentalRequestRepository.findBlockingForVehicleCalendar(eq(vid), any(), any(), any()))
        .thenReturn(List.of(req));

    var dto =
        vehicleOccupancyService.occupancy(vid, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 31));

    assertThat(dto.ranges()).hasSize(2);
    assertThat(dto.ranges().getFirst().source()).isEqualTo(VehicleOccupancySource.rental);
    assertThat(dto.ranges().getFirst().startDate()).isEqualTo(LocalDate.of(2026, 4, 19));
    assertThat(dto.ranges().getFirst().endDate()).isEqualTo(LocalDate.of(2026, 4, 21));
    assertThat(dto.ranges().get(1).source()).isEqualTo(VehicleOccupancySource.rental_request);
    assertThat(dto.ranges().get(1).startDate()).isEqualTo(LocalDate.of(2026, 5, 1));
    assertThat(dto.ranges().get(1).endDate()).isEqualTo(LocalDate.of(2026, 5, 3));
  }

  @Test
  void occupancy_throwsWhenVehicleMissing() {
    Long vid = 1L;
    when(vehicleRepository.findByIdAndDeletedFalse(vid)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                vehicleOccupancyService.occupancy(
                    vid, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
