package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CreateRentalRequest;
import com.algorycode.rent.api.dto.UpdateRentalRequest;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.domain.location.HandoverLocationKind;
import com.algorycode.rent.domain.rental.CustomerSnapshot;
import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalCommissionFlow;
import com.algorycode.rent.domain.rental.RentalStatus;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleStatus;
import com.algorycode.rent.domain.vehicle.VehicleStatusDefinition;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.repository.RentalStatusDefinitionRepository;
import com.algorycode.rent.repository.ReservationExtraOptionTemplateRepository;
import com.algorycode.rent.repository.VehicleOptionDefinitionRepository;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.repository.VehicleStatusDefinitionRepository;
import com.algorycode.rent.service.readmodel.FeFleetSnapshotBuilder;
import com.algorycode.rent.service.support.RentalTestFixtures;
import com.algorycode.rent.service.support.VehicleTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

  @Mock private RentalRepository rentalRepository;
  @Mock private VehicleRepository vehicleRepository;
  @Mock private ObjectStorageService objectStorageService;
  @Mock private CustomerRecordService customerRecordService;
  @Mock private HandoverLocationService handoverLocationService;
  @Mock private VehicleOptionDefinitionRepository vehicleOptionDefinitionRepository;
  @Mock private ReservationExtraOptionTemplateRepository reservationExtraOptionTemplateRepository;
  @Mock private VehicleStatusDefinitionRepository vehicleStatusDefinitionRepository;
  @Mock private RentalStatusDefinitionRepository rentalStatusDefinitionRepository;
  @Mock private FeFleetSnapshotBuilder feFleetSnapshotBuilder;
  @Mock private AuditLog auditLog;

  @InjectMocks private RentalService rentalService;

  @BeforeEach
  void stubRentalStatusDefinitions() {
    when(rentalStatusDefinitionRepository.findByCodeIgnoreCase(anyString()))
        .thenAnswer(
            invocation -> Optional.of(RentalTestFixtures.rentalStatusDefinition(invocation.getArgument(0))));
  }

  @Test
  void list_withoutFilters_usesFindAllByOrder() {
    when(rentalRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

    rentalService.list(null, null, null, null);

    verify(rentalRepository).findAllByOrderByCreatedAtDesc();
  }

  @Test
  void list_withVehicleId_callsVehicleQuery() {
    var vid = 1L;
    when(rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(vid)).thenReturn(List.of());

    rentalService.list(vid, null, null, null);

    verify(rentalRepository).findByVehicle_IdOrderByCreatedAtDesc(vid);
  }

  @Test
  void list_withStatus_callsStatusQuery() {
    when(rentalRepository.findByStatusDefinition_CodeOrderByCreatedAtDesc("pending"))
        .thenReturn(List.of());

    rentalService.list(null, RentalStatus.pending, null, null);

    verify(rentalRepository).findByStatusDefinition_CodeOrderByCreatedAtDesc("pending");
  }

  @Test
  void list_withVehicleIdAndStatus_callsCombinedQuery() {
    var vid = 1L;
    when(rentalRepository.findByVehicle_IdAndStatusDefinition_CodeOrderByCreatedAtDesc(vid, "active"))
        .thenReturn(List.of(sampleRental(vid)));

    var rows = rentalService.list(vid, RentalStatus.active, null, null);

    assertThat(rows).hasSize(1);
    assertThat(rows.getFirst().status()).isEqualTo(RentalStatus.active);
    verify(rentalRepository).findByVehicle_IdAndStatusDefinition_CodeOrderByCreatedAtDesc(vid, "active");
  }

  @Test
  void getById_returnsDto() {
    var id = 1L;
    var r = sampleRental(1L);
    r.setId(id);
    when(rentalRepository.findById(id)).thenReturn(Optional.of(r));

    var dto = rentalService.getById(id);

    assertThat(dto.id()).isEqualTo(id);
    assertThat(dto.customer().fullName()).isEqualTo("Test User");
  }

  @Test
  void getById_throwsWhenMissing() {
    var id = 1L;
    when(rentalRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> rentalService.getById(id))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Rental not found");
  }

  @Test
  void update_whenCompleted_setsVehicleDefaultPickupToReturnHandoverLocation() {
    Long rentalId = 1L;
    Long vehicleId = 1L;

    HandoverLocation returnLoc = new HandoverLocation();
    returnLoc.setId(1L);
    returnLoc.setKind(HandoverLocationKind.RETURN);
    returnLoc.setName("Havalimanı teslim");
    returnLoc.setActive(true);
    returnLoc.setLineOrder(0);

    HandoverLocation oldDefaultPickup = new HandoverLocation();
    oldDefaultPickup.setId(1L);
    oldDefaultPickup.setKind(HandoverLocationKind.PICKUP);
    oldDefaultPickup.setName("Ofis");
    oldDefaultPickup.setActive(true);
    oldDefaultPickup.setLineOrder(0);

    Vehicle vehicle = new Vehicle();
    vehicle.setId(vehicleId);
    vehicle.setPlate("34 T 1");
    VehicleTestFixtures.attachBrandModelStatus(vehicle, "Toyota", "Corolla", VehicleStatus.available);
    vehicle.setYear(2023);
    vehicle.setDefaultPickupHandoverLocation(oldDefaultPickup);
    vehicle.setCreatedAt(Instant.now());
    vehicle.setUpdatedAt(Instant.now());

    Rental rental = new Rental();
    rental.setId(rentalId);
    rental.setVehicle(vehicle);
    rental.setStartDate(LocalDate.of(2026, 4, 1));
    rental.setEndDate(LocalDate.of(2026, 4, 10));
    rental.setReturnHandoverLocation(returnLoc);
    RentalTestFixtures.attachRentalStatus(rental, RentalStatus.active);
    rental.setCommissionAmount(BigDecimal.ZERO);
    rental.setCommissionFlow(RentalCommissionFlow.collect);
    rental.setCreatedAt(Instant.parse("2026-03-01T10:00:00Z"));
    rental.setUpdatedAt(Instant.parse("2026-03-01T10:00:00Z"));

    var customer = new CustomerSnapshot();
    customer.setFullName("Ali Veli");
    customer.setNationalId("11111111111");
    customer.setPassportNo("P1");
    customer.setPhone("+90");
    customer.setDriverLicenseImageDataUrl("dl");
    customer.setPassportImageDataUrl("pp");
    rental.setCustomer(customer);

    when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
    when(rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(vehicleId)).thenReturn(List.of());
    when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(objectStorageService.resolvePublicUrl(any())).thenAnswer(invocation -> invocation.getArgument(0));

    UpdateRentalRequest req =
        new UpdateRentalRequest(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            RentalStatus.completed,
            null,
            null);

    rentalService.update(rentalId, req);

    ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
    verify(vehicleRepository).save(vehicleCaptor.capture());
    assertThat(vehicleCaptor.getValue().getDefaultPickupHandoverLocation()).isSameAs(returnLoc);
  }

  @Test
  void update_whenCompleted_withoutReturnLocation_doesNotSaveVehicle() {
    Long rentalId = 1L;
    Long vehicleId = 1L;
    Vehicle vehicle = new Vehicle();
    vehicle.setId(vehicleId);
    vehicle.setPlate("06 A 2");
    VehicleTestFixtures.attachBrandModelStatus(vehicle, "VW", "Polo", VehicleStatus.available);
    vehicle.setYear(2021);
    vehicle.setCreatedAt(Instant.now());
    vehicle.setUpdatedAt(Instant.now());

    Rental rental = new Rental();
    rental.setId(rentalId);
    rental.setVehicle(vehicle);
    rental.setStartDate(LocalDate.of(2026, 5, 1));
    rental.setEndDate(LocalDate.of(2026, 5, 5));
    rental.setReturnHandoverLocation(null);
    RentalTestFixtures.attachRentalStatus(rental, RentalStatus.active);
    rental.setCommissionAmount(BigDecimal.ZERO);
    rental.setCommissionFlow(RentalCommissionFlow.collect);
    rental.setCreatedAt(Instant.now());
    rental.setUpdatedAt(Instant.now());
    var customer = new CustomerSnapshot();
    customer.setFullName("A");
    customer.setNationalId("1");
    customer.setPassportNo("P");
    customer.setPhone("+90");
    customer.setDriverLicenseImageDataUrl("d");
    customer.setPassportImageDataUrl("p");
    rental.setCustomer(customer);

    when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
    when(rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(vehicleId)).thenReturn(List.of());
    when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(objectStorageService.resolvePublicUrl(any())).thenAnswer(invocation -> invocation.getArgument(0));

    rentalService.update(
        rentalId,
        new UpdateRentalRequest(
            null, null, null, null, null, null, null, RentalStatus.completed, null, null));

    verify(vehicleRepository, never()).save(any());
  }

  @Test
  void updateStatus_whenCancelled_setsCancelled() {
    Rental rental = sampleRental(1L);
    rental.setId(9L);
    when(rentalRepository.findById(9L)).thenReturn(Optional.of(rental));
    when(rentalRepository.existsByVehicle_IdAndStatusDefinition_CodeInAndIdNot(
            vehicleIdOf(rental),
            List.of(RentalStatus.active.name(), RentalStatus.pending.name()),
            rental.getId()))
        .thenReturn(false);
    when(vehicleStatusDefinitionRepository.findByCodeIgnoreCase("available"))
        .thenReturn(Optional.of(statusDefinition("available")));
    when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(objectStorageService.resolvePublicUrl(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var dto = rentalService.updateStatus(9L, RentalStatus.cancelled);

    assertThat(dto.status()).isEqualTo(RentalStatus.cancelled);
    verify(rentalRepository).save(any(Rental.class));
    verify(vehicleRepository).save(any(Vehicle.class));
  }

  @Test
  void updateStatus_whenUnchanged_skipsSave() {
    Rental rental = sampleRental(1L);
    rental.setId(11L);
    RentalTestFixtures.attachRentalStatus(rental, RentalStatus.active);
    when(rentalRepository.findById(11L)).thenReturn(Optional.of(rental));
    when(rentalRepository.existsByVehicle_IdAndStatusDefinition_CodeInAndIdNot(
            vehicleIdOf(rental),
            List.of(RentalStatus.active.name(), RentalStatus.pending.name()),
            rental.getId()))
        .thenReturn(false);
    when(objectStorageService.resolvePublicUrl(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var dto = rentalService.updateStatus(11L, RentalStatus.active);

    assertThat(dto.status()).isEqualTo(RentalStatus.active);
    verify(rentalRepository, never()).save(any());
    verify(vehicleRepository, never()).save(any(Vehicle.class));
  }

  @Test
  void updateStatus_whenPending_setsVehicleRented() {
    Rental rental = sampleRental(1L);
    rental.setId(21L);
    RentalTestFixtures.attachRentalStatus(rental, RentalStatus.active);
    when(rentalRepository.findById(21L)).thenReturn(Optional.of(rental));
    when(rentalRepository.existsByVehicle_IdAndStatusDefinition_CodeInAndIdNot(
            vehicleIdOf(rental),
            List.of(RentalStatus.active.name(), RentalStatus.pending.name()),
            rental.getId()))
        .thenReturn(false);
    when(vehicleStatusDefinitionRepository.findByCodeIgnoreCase("rented"))
        .thenReturn(Optional.of(statusDefinition("rented")));
    when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(objectStorageService.resolvePublicUrl(any())).thenAnswer(invocation -> invocation.getArgument(0));

    rentalService.updateStatus(21L, RentalStatus.pending);

    verify(vehicleRepository).save(any(Vehicle.class));
  }

  @Test
  void create_whenVehicleRented_throwsConflict() {
    Vehicle vehicle = new Vehicle();
    vehicle.setId(1L);
    VehicleTestFixtures.attachBrandModelStatus(vehicle, "Fiat", "Egea", VehicleStatus.rented);
    when(vehicleRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(vehicle));

    CreateRentalRequest req =
        new CreateRentalRequest(
            1L,
            null,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 5),
            null,
            null,
            new CreateRentalRequest.CustomerBody("Ali", "", "P", "+90", null, null, null, null),
            BigDecimal.ZERO,
            RentalCommissionFlow.collect,
            null,
            null,
            null,
            null);

    var vehicle = new Vehicle();
    vehicle.setId(vehicleId);
    vehicle.setPlate("06 X 06");
    VehicleTestFixtures.attachBrandModelStatus(vehicle, "VW", "Golf", VehicleStatus.available);
    vehicle.setYear(2022);
    vehicle.setCreatedAt(Instant.now());
    vehicle.setUpdatedAt(Instant.now());

    var customer = new CustomerSnapshot();
    customer.setFullName("Test User");
    customer.setNationalId("11111111111");
    customer.setPassportNo("P1");
    customer.setPhone("+90");

    var rental = new Rental();
    rental.setId(1L);
    rental.setVehicle(vehicle);
    rental.setStartDate(LocalDate.of(2026, 4, 1));
    rental.setEndDate(LocalDate.of(2026, 4, 10));
    RentalTestFixtures.attachRentalStatus(rental, RentalStatus.active);
    rental.setCustomer(customer);
    rental.setCreatedAt(Instant.parse("2026-03-01T10:00:00Z"));
    rental.setUpdatedAt(Instant.parse("2026-03-01T10:00:00Z"));
    return rental;
  }

  private static VehicleStatusDefinition statusDefinition(String code) {
    var definition = new VehicleStatusDefinition();
    definition.setCode(code);
    return definition;
  }

  private static Long vehicleIdOf(Rental rental) {
    return rental.getVehicle() != null ? rental.getVehicle().getId() : null;
  }
}
