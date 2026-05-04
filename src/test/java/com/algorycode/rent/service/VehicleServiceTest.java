package com.algorycode.rent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.algorycode.rent.api.dto.CreateVehicleRequest;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.domain.rental.CustomerSnapshot;
import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalStatus;
import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.domain.request.RentalRequestStatus;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleBrand;
import com.algorycode.rent.domain.vehicle.VehicleModel;
import com.algorycode.rent.domain.vehicle.VehicleStatus;
import com.algorycode.rent.domain.vehicle.VehicleStatusDefinition;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.repository.RentalRequestRepository;
import com.algorycode.rent.repository.VehicleBodyStyleRepository;
import com.algorycode.rent.repository.VehicleFuelTypeRepository;
import com.algorycode.rent.repository.VehicleModelRepository;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.repository.VehicleStatusDefinitionRepository;
import com.algorycode.rent.repository.VehicleTransmissionTypeRepository;
import com.algorycode.rent.service.readmodel.FeFleetSnapshotBuilder;
import com.algorycode.rent.service.support.RentalTestFixtures;
import com.algorycode.rent.service.support.VehicleAvailabilitySlotAnalyzer;
import com.algorycode.rent.service.support.VehicleTestFixtures;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

  @Mock private VehicleRepository vehicleRepository;
  @Mock private VehicleModelRepository vehicleModelRepository;
  @Mock private VehicleStatusDefinitionRepository vehicleStatusDefinitionRepository;
  @Mock private VehicleBodyStyleRepository vehicleBodyStyleRepository;
  @Mock private VehicleFuelTypeRepository vehicleFuelTypeRepository;
  @Mock private VehicleTransmissionTypeRepository vehicleTransmissionTypeRepository;
  @Mock private ObjectStorageService objectStorageService;
  @Mock private HandoverLocationService handoverLocationService;
  @Mock private VehicleOptionTemplateService vehicleOptionTemplateService;
  @Mock private RentalRepository rentalRepository;
  @Mock private RentalRequestRepository rentalRequestRepository;
  @Mock private VehicleImageService vehicleImageService;
  @Mock private MessageSource messageSource;

  private VehicleService vehicleService;

  @BeforeEach
  void setUp() {
    lenient()
        .when(
            rentalRequestRepository.findPotentiallyBlockingRequestsForAvailability(
                any(), any(), anyList()))
        .thenReturn(Collections.emptyList());
    lenient()
        .when(messageSource.getMessage(any(), any(), any()))
        .thenAnswer(invocation -> invocation.getArgument(0).toString());
    vehicleService =
        new VehicleService(
            vehicleRepository,
            vehicleModelRepository,
            vehicleStatusDefinitionRepository,
            vehicleBodyStyleRepository,
            vehicleFuelTypeRepository,
            vehicleTransmissionTypeRepository,
            objectStorageService,
            handoverLocationService,
            vehicleOptionTemplateService,
            new VehicleAvailabilityService(
                vehicleRepository,
                rentalRepository,
                rentalRequestRepository,
                new VehicleAvailabilitySlotAnalyzer()),
            vehicleImageService,
            mock(AuditLog.class),
            mock(FeFleetSnapshotBuilder.class),
            messageSource);
  }

  @Test
  void listWithAvailabilityFilter_excludesWhenBufferDayBlocked() {
    var v = sampleVehicle();
    Long vid = v.getId();
    when(vehicleRepository.findAllByDeletedFalse()).thenReturn(List.of(v));
    Rental r = new Rental();
    r.setStartDate(LocalDate.of(2026, 6, 15));
    r.setEndDate(LocalDate.of(2026, 6, 16));
    RentalTestFixtures.attachRentalStatus(r, RentalStatus.active);
    Vehicle rv = new Vehicle();
    rv.setId(vid);
    r.setVehicle(rv);
    CustomerSnapshot c = new CustomerSnapshot();
    c.setFullName("Ali");
    r.setCustomer(c);
    when(rentalRepository.findPotentiallyBlockingForAvailability(
            LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 15)))
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
            LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 15)))
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
            LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 12)))
        .thenReturn(Collections.emptyList());

    RentalRequest req = new RentalRequest();
    req.setStatus(RentalRequestStatus.pending);
    req.setStartDate(LocalDate.of(2026, 8, 10));
    req.setEndDate(LocalDate.of(2026, 8, 11));
    Vehicle rv = new Vehicle();
    rv.setId(vid);
    req.setVehicle(rv);

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
            LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 16)))
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
    RentalTestFixtures.attachRentalStatus(r, RentalStatus.active);
    Vehicle rv = new Vehicle();
    rv.setId(vid);
    r.setVehicle(rv);
    CustomerSnapshot c = new CustomerSnapshot();
    c.setFullName("Ali");
    r.setCustomer(c);
    when(rentalRepository.findPotentiallyBlockingForAvailability(
            LocalDate.of(2026, 6, 14), LocalDate.of(2026, 7, 1)))
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
  void create_whenVehicleModelIdMissing_usesFirstModel() {
    var model = new VehicleModel();
    model.setId(11L);
    var brand = new VehicleBrand();
    brand.setName("Genel");
    model.setBrand(brand);
    model.setName("—");
    var available = new VehicleStatusDefinition();
    available.setId(1L);
    available.setCode("available");
    when(vehicleModelRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(model));
    when(vehicleStatusDefinitionRepository.findByCodeIgnoreCase("available"))
        .thenReturn(Optional.of(available));
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
            CreateVehicleRequest.builder().plate("34 TEST 34").countryCode("TR").build());

    assertThat(createdId).isEqualTo(999L);
    verify(vehicleModelRepository).findFirstByOrderByIdAsc();
  }

  private static Vehicle sampleVehicle() {
    var v = new Vehicle();
    v.setId(301L);
    v.setPlate("34 ABC 101");
    VehicleTestFixtures.attachBrandModelStatus(v, "Toyota", "Corolla", VehicleStatus.available);
    v.setYear(2023);
    v.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
    v.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
    return v;
  }
}
