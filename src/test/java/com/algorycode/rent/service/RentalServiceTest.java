package com.algorycode.rent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.algorycode.rent.dto.CreateRentalRequest;
import com.algorycode.rent.dto.CustomerRequest;
import com.algorycode.rent.dto.UpdateRentalRequest;
import com.algorycode.rent.exception.BadRequestException;
import com.algorycode.rent.exception.ResourceNotFoundException;
import com.algorycode.rent.entity.HandoverLocation;
import com.algorycode.rent.entity.HandoverLocationKind;
import com.algorycode.rent.entity.Customer;
import com.algorycode.rent.entity.Rental;
import com.algorycode.rent.entity.RentalCommissionFlow;
import com.algorycode.rent.entity.RentalStatus;
import com.algorycode.rent.entity.Vehicle;
import com.algorycode.rent.entity.VehicleStatus;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.repository.CustomerRepository;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.repository.ReservationExtraOptionTemplateRepository;
import com.algorycode.rent.repository.VehicleOptionDefinitionRepository;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.service.support.RentalTestFixtures;
import com.algorycode.rent.service.support.VehicleTestFixtures;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

  @Mock private RentalRepository rentalRepository;
  @Mock private VehicleRepository vehicleRepository;
  @Mock private ObjectStorageService objectStorageService;
  @Mock private CustomerRecordService customerRecordService;
  @Mock private CustomerRepository customerRepository;
  @Mock private CustomerService customerService;
  @Mock private HandoverLocationService handoverLocationService;
  @Mock private VehicleOptionDefinitionRepository vehicleOptionDefinitionRepository;
  @Mock private ReservationExtraOptionTemplateRepository reservationExtraOptionTemplateRepository;
  @Mock private AuditLog auditLog;
  @Mock private MessageSource messageSource;
  @Mock private VehicleCatalogStatusService vehicleCatalogStatusService;

  @InjectMocks private RentalService rentalService;

  @BeforeEach
  void stubVehicleCatalogStatus() {
    lenient()
        .doNothing()
        .when(vehicleCatalogStatusService)
        .updateVehicleStatus(anyLong(), any(VehicleStatus.class));
    lenient()
        .when(customerRepository.save(any(Customer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    lenient()
        .when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
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
    when(rentalRepository.findByRentalStatusOrderByCreatedAtDesc(RentalStatus.PENDING))
        .thenReturn(List.of());

    rentalService.list(null, "pending", null, null);

    verify(rentalRepository).findByRentalStatusOrderByCreatedAtDesc(RentalStatus.PENDING);
  }

  @Test
  void list_withVehicleIdAndStatus_callsCombinedQuery() {
    var vid = 1L;
    when(rentalRepository.findByVehicle_IdAndRentalStatusOrderByCreatedAtDesc(
            vid, RentalStatus.ACTIVE))
        .thenReturn(List.of(sampleRental(vid)));

    var rows = rentalService.list(vid, "active", null, null);

    assertThat(rows).hasSize(1);
    assertThat(rows.getFirst().status()).isEqualTo(RentalStatus.ACTIVE);
    verify(rentalRepository)
        .findByVehicle_IdAndRentalStatusOrderByCreatedAtDesc(vid, RentalStatus.ACTIVE);
  }

  @Test
  void getById_returnsDto() {
    var id = 1L;
    var r = sampleRental(1L);
    r.setId(id);
    when(rentalRepository.findDetailById(id)).thenReturn(Optional.of(r));

    var dto = rentalService.getById(id);

    assertThat(dto.id()).isEqualTo(id);
    assertThat(dto.customer().fullName()).isEqualTo("Test User");
  }

  @Test
  void getById_throwsWhenMissing() {
    var id = 1L;
    when(rentalRepository.findDetailById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> rentalService.getById(id))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Rental not found");
  }

  @Test
  void create_whenCustomerExists_loadsCustomerWithoutCreating() {
    Long vehicleId = 1L;
    Long customerId = 42L;
    Vehicle v = new Vehicle();
    v.setId(vehicleId);
    v.setRentalDailyPrice(BigDecimal.valueOf(100));
    VehicleTestFixtures.attachBrandModelStatus(v, "VW", "Golf", VehicleStatus.ACTIVE);
    v.setCreatedAt(Instant.now());
    v.setUpdatedAt(Instant.now());

    Customer customer =
        Customer.builder()
            .fullName("Existing")
            .nationalId("11111111111")
            .passportNo("P1")
            .phone("+90")
            .build();
    customer.setId(customerId);
    customer.setPassportImageDataUrl("data:image/png;base64,x");
    customer.setDriverLicenseImageDataUrl("data:image/png;base64,y");

    when(vehicleRepository.findByIdAndDeletedFalse(vehicleId)).thenReturn(Optional.of(v));
    when(rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(vehicleId)).thenReturn(List.of());
    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
    when(rentalRepository.save(any(Rental.class)))
        .thenAnswer(
            invocation -> {
              Rental r = invocation.getArgument(0);
              if (r.getId() == null) {
                r.setId(500L);
              }
              return r;
            });
    when(objectStorageService.uploadDataUrl(anyString(), anyString(), anyString()))
        .thenAnswer(invocation -> invocation.getArgument(2));
    when(objectStorageService.resolvePublicUrl(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    CreateRentalRequest req =
        new CreateRentalRequest(
            vehicleId,
            null,
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 5),
            null,
            null,
            customerId,
            null,
            null,
            null,
            null);

    var dto = rentalService.create(req);

    assertThat(dto.customer().fullName()).isEqualTo("Existing");
    verify(customerRepository).findById(customerId);
    verify(customerService, never()).createCustomer(any(CustomerRequest.class));
    verify(vehicleCatalogStatusService).updateVehicleStatus(vehicleId, VehicleStatus.RENTED);
  }

  @Test
  void create_whenVehicleMaintenance_throws() {
    Long vehicleId = 55L;
    Long customerId = 1L;
    Vehicle v = new Vehicle();
    v.setId(vehicleId);
    v.setRentalDailyPrice(BigDecimal.TEN);
    VehicleTestFixtures.attachBrandModelStatus(v, "VW", "Golf", VehicleStatus.MAINTENANCE);
    v.setCreatedAt(Instant.now());
    v.setUpdatedAt(Instant.now());

    when(vehicleRepository.findByIdAndDeletedFalse(vehicleId)).thenReturn(Optional.of(v));

    CreateRentalRequest req =
        new CreateRentalRequest(
            vehicleId,
            null,
            LocalDate.of(2026, 8, 1),
            LocalDate.of(2026, 8, 3),
            null,
            null,
            customerId,
            null,
            null,
            null,
            null);

    assertThatThrownBy(() -> rentalService.create(req)).isInstanceOf(BadRequestException.class);
    verify(vehicleRepository, never()).save(any(Vehicle.class));
  }

  @Test
  void create_whenCustomerMissing_throws() {
    Long vehicleId = 3L;
    Long customerId = 99L;
    Vehicle v = new Vehicle();
    v.setId(vehicleId);
    v.setRentalDailyPrice(BigDecimal.valueOf(50));
    VehicleTestFixtures.attachBrandModelStatus(v, "Fiat", "Egea", VehicleStatus.ACTIVE);
    v.setCreatedAt(Instant.now());
    v.setUpdatedAt(Instant.now());

    when(vehicleRepository.findByIdAndDeletedFalse(vehicleId)).thenReturn(Optional.of(v));
    when(rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(vehicleId)).thenReturn(List.of());
    when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

    CreateRentalRequest req =
        new CreateRentalRequest(
            vehicleId,
            null,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 3),
            null,
            null,
            customerId,
            null,
            null,
            null,
            null);

    assertThatThrownBy(() -> rentalService.create(req))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Customer not found");
    verify(customerService, never()).createCustomer(any(CustomerRequest.class));
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
    VehicleTestFixtures.attachBrandModelStatus(
        vehicle, "Toyota", "Corolla", VehicleStatus.ACTIVE);
    vehicle.setYear(2023);
    vehicle.setDefaultPickupHandoverLocation(oldDefaultPickup);
    vehicle.setCreatedAt(Instant.now());
    vehicle.setUpdatedAt(Instant.now());

    Rental rental = new Rental();
    rental.setId(rentalId);
    rental.setVehicleId(vehicleId);
    rental.setVehicle(vehicle);
    rental.setStartDate(LocalDate.of(2026, 4, 1));
    rental.setEndDate(LocalDate.of(2026, 4, 10));
    rental.setReturnHandoverLocation(returnLoc);
    RentalTestFixtures.attachRentalStatus(rental, RentalStatus.ACTIVE);
    rental.setCommissionAmount(BigDecimal.ZERO);
    rental.setCommissionFlow(RentalCommissionFlow.collect);
    rental.setCreatedAt(Instant.parse("2026-03-01T10:00:00Z"));
    rental.setUpdatedAt(Instant.parse("2026-03-01T10:00:00Z"));

    var customer =
        Customer.builder()
            .fullName("Ali Veli")
            .nationalId("11111111111")
            .passportNo("P1")
            .phone("+90")
            .build();
    customer.setId(1L);
    rental.setCustomerId(1L);
    rental.setCustomer(customer);

    when(rentalRepository.findDetailById(rentalId)).thenReturn(Optional.of(rental));
    when(rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(vehicleId)).thenReturn(List.of());
    when(rentalRepository.save(any(Rental.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(objectStorageService.resolvePublicUrl(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UpdateRentalRequest req =
        new UpdateRentalRequest(
            null, null, null, null, null, null, "completed", null, null, null);

    rentalService.update(rentalId, req);

    verify(vehicleCatalogStatusService).updateVehicleStatus(vehicleId, VehicleStatus.ACTIVE);
    verify(vehicleRepository).save(any(Vehicle.class));
  }

  @Test
  void update_whenCompleted_withoutReturnLocation_setsVehicleAvailable() {
    Long rentalId = 1L;
    Long vehicleId = 1L;
    Vehicle vehicle = new Vehicle();
    vehicle.setId(vehicleId);
    vehicle.setPlate("06 A 2");
    VehicleTestFixtures.attachBrandModelStatus(vehicle, "VW", "Polo", VehicleStatus.ACTIVE);
    vehicle.setYear(2021);
    vehicle.setCreatedAt(Instant.now());
    vehicle.setUpdatedAt(Instant.now());

    Rental rental = new Rental();
    rental.setId(rentalId);
    rental.setVehicleId(vehicleId);
    rental.setVehicle(vehicle);
    rental.setStartDate(LocalDate.of(2026, 5, 1));
    rental.setEndDate(LocalDate.of(2026, 5, 5));
    rental.setReturnHandoverLocation(null);
    RentalTestFixtures.attachRentalStatus(rental, RentalStatus.ACTIVE);
    rental.setCommissionAmount(BigDecimal.ZERO);
    rental.setCommissionFlow(RentalCommissionFlow.collect);
    rental.setCreatedAt(Instant.now());
    rental.setUpdatedAt(Instant.now());
    var customer =
        Customer.builder().fullName("A").nationalId("1").passportNo("P").phone("+90").build();
    customer.setId(1L);
    rental.setCustomerId(1L);
    rental.setCustomer(customer);
    customer.setDriverLicenseImageDataUrl("d");
    customer.setPassportImageDataUrl("p");

    when(rentalRepository.findDetailById(rentalId)).thenReturn(Optional.of(rental));
    when(rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(vehicleId)).thenReturn(List.of());
    when(rentalRepository.save(any(Rental.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(objectStorageService.resolvePublicUrl(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    rentalService.update(
        rentalId,
        new UpdateRentalRequest(
            null, null, null, null, null, null, "completed", null, null, null));

    verify(vehicleCatalogStatusService).updateVehicleStatus(vehicleId, VehicleStatus.ACTIVE);
  }

  @Test
  void updateStatus_whenCancelled_setsCancelled() {
    Rental rental = sampleRental(1L);
    rental.setId(9L);
    when(rentalRepository.findDetailById(9L)).thenReturn(Optional.of(rental));
    when(rentalRepository.save(any(Rental.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(objectStorageService.resolvePublicUrl(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var dto = rentalService.updateStatus(9L, "cancelled");

    assertThat(dto.status()).isEqualTo(RentalStatus.CANCELLED);
    verify(rentalRepository).save(any(Rental.class));
    verify(vehicleCatalogStatusService).updateVehicleStatus(1L, VehicleStatus.ACTIVE);
  }

  @Test
  void updateStatus_whenUnchanged_skipsSave() {
    Rental rental = sampleRental(1L);
    rental.setId(11L);
    RentalTestFixtures.attachRentalStatus(rental, RentalStatus.ACTIVE);
    when(rentalRepository.findDetailById(11L)).thenReturn(Optional.of(rental));
    when(objectStorageService.resolvePublicUrl(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var dto = rentalService.updateStatus(11L, "active");

    assertThat(dto.status()).isEqualTo(RentalStatus.ACTIVE);
    verify(rentalRepository, never()).save(any());
    verify(vehicleCatalogStatusService, never())
        .updateVehicleStatus(anyLong(), any(VehicleStatus.class));
  }

  @Test
  void updateStatus_whenPending_setsVehicleRented() {
    Rental rental = sampleRental(1L);
    rental.setId(21L);
    RentalTestFixtures.attachRentalStatus(rental, RentalStatus.ACTIVE);
    when(rentalRepository.findDetailById(21L)).thenReturn(Optional.of(rental));
    when(rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
    when(rentalRepository.save(any(Rental.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(objectStorageService.resolvePublicUrl(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    rentalService.updateStatus(21L, "pending");

    verify(vehicleCatalogStatusService).updateVehicleStatus(1L, VehicleStatus.RENTED);
  }

  private Rental sampleRental(Long vehicleIdParam) {
    Vehicle v = new Vehicle();
    v.setId(vehicleIdParam);
    v.setPlate("06 X 06");
    v.setRentalDailyPrice(BigDecimal.valueOf(100));
    VehicleTestFixtures.attachBrandModelStatus(v, "VW", "Golf", VehicleStatus.ACTIVE);
    v.setYear(2022);
    v.setCreatedAt(Instant.now());
    v.setUpdatedAt(Instant.now());

    Customer customer =
        Customer.builder()
            .fullName("Test User")
            .nationalId("11111111111")
            .passportNo("P1")
            .phone("+90")
            .build();
    customer.setId(1L);

    Rental rental = new Rental();
    rental.setVehicleId(vehicleIdParam);
    rental.setVehicle(v);
    rental.setStartDate(LocalDate.of(2026, 4, 1));
    rental.setEndDate(LocalDate.of(2026, 4, 10));
    RentalTestFixtures.attachRentalStatus(rental, RentalStatus.ACTIVE);
    rental.setCustomerId(1L);
    rental.setCustomer(customer);
    rental.setCreatedAt(Instant.parse("2026-03-01T10:00:00Z"));
    rental.setUpdatedAt(Instant.parse("2026-03-01T10:00:00Z"));
    return rental;
  }
}
