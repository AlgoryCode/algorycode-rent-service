package com.algorycode.rent.service;

import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.rental.CustomerSnapshot;
import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalStatus;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

  @Mock private RentalRepository rentalRepository;
  @Mock private VehicleRepository vehicleRepository;
  @Mock private ObjectStorageService objectStorageService;
  @Mock private CustomerRecordService customerRecordService;

  @InjectMocks private RentalService rentalService;

  @Test
  void list_withoutFilters_usesFindAllByOrder() {
    when(rentalRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

    rentalService.list(null, null, null, null);

    verify(rentalRepository).findAllByOrderByCreatedAtDesc();
  }

  @Test
  void list_withVehicleId_callsVehicleQuery() {
    var vid = UUID.randomUUID();
    when(rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(vid)).thenReturn(List.of());

    rentalService.list(vid, null, null, null);

    verify(rentalRepository).findByVehicle_IdOrderByCreatedAtDesc(vid);
  }

  @Test
  void list_withStatus_callsStatusQuery() {
    when(rentalRepository.findByStatusOrderByCreatedAtDesc(RentalStatus.pending))
        .thenReturn(List.of());

    rentalService.list(null, RentalStatus.pending, null, null);

    verify(rentalRepository).findByStatusOrderByCreatedAtDesc(RentalStatus.pending);
  }

  @Test
  void list_withVehicleIdAndStatus_callsCombinedQuery() {
    var vid = UUID.randomUUID();
    when(rentalRepository.findByVehicle_IdAndStatusOrderByCreatedAtDesc(vid, RentalStatus.active))
        .thenReturn(List.of(sampleRental(vid)));

    var rows = rentalService.list(vid, RentalStatus.active, null, null);

    assertThat(rows).hasSize(1);
    assertThat(rows.getFirst().status()).isEqualTo(RentalStatus.active);
    verify(rentalRepository).findByVehicle_IdAndStatusOrderByCreatedAtDesc(vid, RentalStatus.active);
  }

  @Test
  void getById_returnsDto() {
    var id = UUID.randomUUID();
    var r = sampleRental(UUID.randomUUID());
    r.setId(id);
    when(rentalRepository.findById(id)).thenReturn(Optional.of(r));

    var dto = rentalService.getById(id);

    assertThat(dto.id()).isEqualTo(id);
    assertThat(dto.customer().fullName()).isEqualTo("Test User");
  }

  @Test
  void getById_throwsWhenMissing() {
    var id = UUID.randomUUID();
    when(rentalRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> rentalService.getById(id))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Rental not found");
  }

  private static Rental sampleRental(UUID vehicleId) {
    var vehicle = new Vehicle();
    vehicle.setId(vehicleId);
    vehicle.setPlate("06 X 06");
    vehicle.setBrand("VW");
    vehicle.setModel("Golf");
    vehicle.setYear(2022);
    vehicle.setMaintenance(false);
    vehicle.setCreatedAt(Instant.now());
    vehicle.setUpdatedAt(Instant.now());

    var customer = new CustomerSnapshot();
    customer.setFullName("Test User");
    customer.setNationalId("11111111111");
    customer.setPassportNo("P1");
    customer.setPhone("+90");

    var rental = new Rental();
    rental.setId(UUID.randomUUID());
    rental.setVehicle(vehicle);
    rental.setStartDate(LocalDate.of(2026, 4, 1));
    rental.setEndDate(LocalDate.of(2026, 4, 10));
    rental.setStatus(RentalStatus.active);
    rental.setCustomer(customer);
    rental.setCreatedAt(Instant.parse("2026-03-01T10:00:00Z"));
    rental.setUpdatedAt(Instant.parse("2026-03-01T10:00:00Z"));
    return rental;
  }
}
