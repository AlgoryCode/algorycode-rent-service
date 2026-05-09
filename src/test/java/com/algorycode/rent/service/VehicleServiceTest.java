package com.algorycode.rent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.algorycode.rent.dto.CreateVehicleRequest;
import com.algorycode.rent.exception.BadRequestException;
import com.algorycode.rent.exception.ResourceNotFoundException;
import com.algorycode.rent.entity.HandoverLocation;
import com.algorycode.rent.entity.Customer;
import com.algorycode.rent.entity.Rental;
import com.algorycode.rent.entity.RentalStatus;
import com.algorycode.rent.entity.RentalRequest;
import com.algorycode.rent.entity.RentalRequestStatus;
import com.algorycode.rent.entity.Vehicle;
import com.algorycode.rent.entity.VehicleStatus;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.repository.HandoverLocationRepository;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.repository.RentalRequestRepository;
import com.algorycode.rent.repository.VehicleOptionTemplateRepository;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.service.support.RentalTestFixtures;
import com.algorycode.rent.service.support.VehicleAvailabilitySlotAnalyzer;
import com.algorycode.rent.service.support.VehicleTestFixtures;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

  @Mock private VehicleRepository vehicleRepository;
  @Mock private HandoverLocationRepository handoverLocationRepository;
  @Mock private VehicleOptionTemplateRepository vehicleOptionTemplateRepository;
  @Mock private ObjectStorageService objectStorageService;
  @Mock private RentalRepository rentalRepository;
  @Mock private RentalRequestRepository rentalRequestRepository;
  @Mock private VehicleImageService vehicleImageService;
  @Mock private ApplicationEventPublisher applicationEventPublisher;
  @Mock private VehicleCatalogStatusService vehicleCatalogStatusService;

  private VehicleService vehicleService;

  @BeforeEach
  void setUp() {
    lenient()
        .when(
            rentalRequestRepository.findPotentiallyBlockingRequestsForAvailability(
                any(), any(), anyList()))
        .thenReturn(Collections.emptyList());
    vehicleService =
        new VehicleService(
            vehicleRepository,
            handoverLocationRepository,
            vehicleOptionTemplateRepository,
            objectStorageService,
            new VehicleAvailabilityService(
                vehicleRepository,
                rentalRepository,
                rentalRequestRepository,
                new VehicleAvailabilitySlotAnalyzer()),
            vehicleImageService,
            mock(AuditLog.class),
            applicationEventPublisher,
            vehicleCatalogStatusService);
  }

  @Test
  void listWithAvailabilityFilter_excludesWhenBufferDayBlocked() {
    var v = sampleVehicle();
    Long vid = v.getId();
    when(vehicleRepository.findAllByDeletedFalse()).thenReturn(List.of(v));
    Rental r = new Rental();
    r.setStartDate(LocalDate.of(2026, 6, 15));
    r.setEndDate(LocalDate.of(2026, 6, 16));
    RentalTestFixtures.attachRentalStatus(r, RentalStatus.ACTIVE);
    r.setVehicleId(vid);
    Customer c =
        Customer.builder()
            .fullName("Ali")
            .nationalId("1")
            .passportNo("")
            .phone("+90")
            .build();
    c.setId(1L);
    r.setCustomerId(1L);
    r.setCustomer(c);
    when(rentalRepository.findPotentiallyBlockingForAvailability(
            LocalDate.of(2026, 6, 14),
            LocalDate.of(2026, 6, 15),
            RentalStatus.CANCELLED))
        .thenReturn(List.of(r));

    var rows =
        vehicleService.listWithAvailabilityFilter(
            LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 14), null, null, false);

    assertThat(rows).isEmpty();
  }

  @Test
  void listWithAvailabilityFilter_includesWhenNoBlockingRental() {
    var v = sampleVehicle();
    when(vehicleRepository.findAllByDeletedFalse()).thenReturn(List.of(v));
    when(rentalRepository.findPotentiallyBlockingForAvailability(
            LocalDate.of(2026, 6, 14),
            LocalDate.of(2026, 6, 15),
            RentalStatus.CANCELLED))
        .thenReturn(Collections.emptyList());

    var rows =
        vehicleService.listWithAvailabilityFilter(
            LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 14), null, null, false);

    assertThat(rows).hasSize(1);
  }

  @Test
  void listWithAvailabilityFilter_excludesWhenPendingRentalRequestOverlaps() {
    var v = sampleVehicle();
    Long vid = v.getId();
    when(vehicleRepository.findAllByDeletedFalse()).thenReturn(List.of(v));
    when(rentalRepository.findPotentiallyBlockingForAvailability(
            LocalDate.of(2026, 8, 10),
            LocalDate.of(2026, 8, 12),
            RentalStatus.CANCELLED))
        .thenReturn(Collections.emptyList());

    RentalRequest req = new RentalRequest();
    req.setStatus(RentalRequestStatus.pending);
    req.setStartDate(LocalDate.of(2026, 8, 10));
    req.setEndDate(LocalDate.of(2026, 8, 11));
    req.setVehicleId(vid);

    when(rentalRequestRepository.findPotentiallyBlockingRequestsForAvailability(
            eq(LocalDate.of(2026, 8, 10)), eq(LocalDate.of(2026, 8, 12)), anyList()))
        .thenReturn(List.of(req));

    var rows =
        vehicleService.listWithAvailabilityFilter(
            LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 11), null, null, false);

    assertThat(rows).isEmpty();
  }

  /**
   * Teslim noktası filtresi varken araçta izinli RETURN listesi boşsa (dashboard’da kısıt yok),
   * uygunluk listesinde yine de gösterilmeli — kiralama oluşturma ile aynı kural.
   */
  @Test
  void listWithAvailabilityFilter_includesVehicleWhenReturnFilterSetButNoReturnRestrictions() {
    var v = sampleVehicle();
    HandoverLocation pickup = mock(HandoverLocation.class);
    Long pickupId = 100L;
    when(pickup.getId()).thenReturn(pickupId);
    v.setDefaultPickupHandoverLocation(pickup);

    when(vehicleRepository.findAllByDeletedFalse()).thenReturn(List.of(v));
    when(rentalRepository.findPotentiallyBlockingForAvailability(
            LocalDate.of(2026, 6, 14),
            LocalDate.of(2026, 6, 16),
            RentalStatus.CANCELLED))
        .thenReturn(Collections.emptyList());

    Long anyReturn = 200L;

    var rows =
        vehicleService.listWithAvailabilityFilter(
            LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 15), pickupId, anyReturn, false);

    assertThat(rows).hasSize(1);
  }

  @Test
  void listWithAvailabilityFilter_throwsWhenOnlyOneDate() {
    assertThatThrownBy(
            () ->
                vehicleService.listWithAvailabilityFilter(
                    LocalDate.of(2026, 6, 14), null, null, null, false))
        .isInstanceOf(BadRequestException.class);
  }

  /**
   * Uzun aralıkta ortada kiralama varken, alış + ertesi gün boşsa kısmi modda listede kalır; sıkı
   * modda elenir.
   */
  @Test
  void listWithAvailabilityFilter_partialIncludesWhenStartWindowFree() {
    var v = sampleVehicle();
    Long vid = v.getId();
    when(vehicleRepository.findAllByDeletedFalse()).thenReturn(List.of(v));

    Rental r = new Rental();
    r.setStartDate(LocalDate.of(2026, 6, 20));
    r.setEndDate(LocalDate.of(2026, 6, 22));
    RentalTestFixtures.attachRentalStatus(r, RentalStatus.ACTIVE);
    r.setVehicleId(vid);
    Customer c =
        Customer.builder()
            .fullName("Ali")
            .nationalId("1")
            .passportNo("")
            .phone("+90")
            .build();
    c.setId(1L);
    r.setCustomerId(1L);
    r.setCustomer(c);
    when(rentalRepository.findPotentiallyBlockingForAvailability(
            LocalDate.of(2026, 6, 14),
            LocalDate.of(2026, 7, 1),
            RentalStatus.CANCELLED))
        .thenReturn(List.of(r));

    var strictOnly =
        vehicleService.listWithAvailabilityFilter(
            LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 30), null, null, false);
    assertThat(strictOnly).isEmpty();

    var withPartial =
        vehicleService.listWithAvailabilityFilter(
            LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 30), null, null, true);
    assertThat(withPartial).hasSize(1);
  }

  @Test
  void listAll_mapsAllVehicles() {
    var v = sampleVehicle();
    when(vehicleRepository.findAllByDeletedFalse()).thenReturn(List.of(v));

    var result = vehicleService.listAll();

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().plate()).isEqualTo("34 ABC 101");
    assertThat(result.getFirst().brand()).isEqualTo("Toyota");
  }

  @Test
  void getById_returnsDtoWhenFound() {
    long id = 55L;
    var v = sampleVehicle();
    v.setId(id);
    when(vehicleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(v));

    var dto = vehicleService.getById(id);

    assertThat(dto.id()).isEqualTo(id);
  }

  @Test
  void getById_throwsWhenMissing() {
    long id = 77L;
    when(vehicleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> vehicleService.getById(id))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Vehicle not found");
  }

  @Test
  void create_persistsVehicle() {
    when(vehicleRepository.save(any(Vehicle.class)))
        .thenAnswer(
            invocation -> {
              Vehicle vehicle = invocation.getArgument(0);
              if (vehicle.getId() == null) {
                vehicle.setId(999L);
              }
              return vehicle;
            });

    Long createdId =
        vehicleService.create(
            CreateVehicleRequest.builder()
                .plate("34 TEST 34")
                .vehicleModelId(1L)
                .year(2024)
                .external(false)
                .rentalDailyPrice(BigDecimal.TEN)
                .countryCode("TR")
                .engine("1.6")
                .fuelTypeId(2L)
                .bodyColor("white")
                .seats(5)
                .luggage(3)
                .transmissionTypeId(1L)
                .bodyStyleId(1L)
                .returnHandoverLocationIds(List.of())
                .optionTemplateIds(List.of())
                .optionDefinitions(List.of())
                .highlights(List.of())
                .images(Map.of())
                .build());

    assertThat(createdId).isEqualTo(999L);
    verify(vehicleRepository, atLeastOnce())
        .save(
            argThat(
                veh ->
                    Objects.equals(veh.getVehicleModelId(), 1L)
                        && veh.getStatus() == VehicleStatus.ACTIVE
                        && Objects.equals(veh.getFuelTypeId(), 2L)
                        && Objects.equals(veh.getTransmissionTypeId(), 1L)
                        && Objects.equals(veh.getBodyStyleId(), 1L)));
  }

  @Test
  void updateVehicleStatus_delegatesToCatalogAndReturnsDto() {
    var v = sampleVehicle();
    doNothing()
        .when(vehicleCatalogStatusService)
        .updateVehicleStatus(any(), any(VehicleStatus.class));
    when(vehicleRepository.findByIdAndDeletedFalse(v.getId())).thenReturn(Optional.of(v));

    var dto = vehicleService.updateVehicleStatus(v.getId(), VehicleStatus.RENTED);

    assertThat(dto.id()).isEqualTo(v.getId());
    verify(vehicleCatalogStatusService).updateVehicleStatus(v.getId(), VehicleStatus.RENTED);
  }

  private static Vehicle sampleVehicle() {
    var v = new Vehicle();
    v.setId(301L);
    v.setPlate("34 ABC 101");
    VehicleTestFixtures.attachBrandModelStatus(v, "Toyota", "Corolla", VehicleStatus.ACTIVE);
    v.setYear(2023);
    v.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
    v.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
    return v;
  }
}
