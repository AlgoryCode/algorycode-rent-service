package com.algorycode.rent.service;

import com.algorycode.rent.dto.CreateRentalRequest;
import com.algorycode.rent.dto.RentalDto;
import com.algorycode.rent.dto.UpdateRentalRequest;
import com.algorycode.rent.exception.BadRequestException;
import com.algorycode.rent.exception.ConflictException;
import com.algorycode.rent.exception.ResourceNotFoundException;
import com.algorycode.rent.mapper.RentalMapper;
import com.algorycode.rent.entity.HandoverLocation;
import com.algorycode.rent.entity.HandoverLocationKind;
import com.algorycode.rent.entity.Customer;
import com.algorycode.rent.entity.Rental;
import com.algorycode.rent.entity.RentalAdditionalDriver;
import com.algorycode.rent.entity.RentalCommissionFlow;
import com.algorycode.rent.entity.RentalOption;
import com.algorycode.rent.entity.RentalStatus;
import com.algorycode.rent.entity.RentalStatusDefinition;
import com.algorycode.rent.entity.Vehicle;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.logging.SafeReasonCodes;
import com.algorycode.rent.repository.CustomerRepository;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.repository.RentalStatusDefinitionRepository;
import com.algorycode.rent.repository.ReservationExtraOptionTemplateRepository;
import com.algorycode.rent.repository.VehicleOptionDefinitionRepository;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.service.support.DateRangeValidator;
import com.algorycode.rent.service.support.RentalCommissionFromVehicle;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalService {

  private final RentalRepository rentalRepository;
  private final RentalStatusDefinitionRepository rentalStatusDefinitionRepository;
  private final VehicleRepository vehicleRepository;
  private final CustomerRepository customerRepository;
  private final ObjectStorageService objectStorageService;
  private final CustomerRecordService customerRecordService;
  private final CustomerService customerService;
  private final HandoverLocationService handoverLocationService;
  private final VehicleOptionDefinitionRepository vehicleOptionDefinitionRepository;
  private final ReservationExtraOptionTemplateRepository reservationExtraOptionTemplateRepository;
  private final AuditLog auditLog;
  private final MessageSource messageSource;

  @Transactional(readOnly = true)
  public List<RentalDto> list(
      Long vehicleId, String statusRaw, LocalDate startDate, LocalDate endDate) {
    DateRangeValidator.requireEndNotBeforeStartIfBothPresent(startDate, endDate);
    RentalStatus status = null;
    if (statusRaw != null && !statusRaw.isBlank()) {
      status = parseRentalStatusRequired(statusRaw);
    }

    List<Rental> base = findRentalsForListFilter(vehicleId, status);

    return base.stream()
        .filter(r -> overlapsRange(r.getStartDate(), r.getEndDate(), startDate, endDate))
        .map(r -> RentalMapper.toDto(r, objectStorageService::resolvePublicUrl))
        .toList();
  }

  @Transactional(readOnly = true)
  public RentalDto getById(Long id) {
    var r =
        rentalRepository
            .findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rental not found: " + id));
    return RentalMapper.toDto(r, objectStorageService::resolvePublicUrl);
  }

  @Transactional
  public RentalDto create(CreateRentalRequest req) {
    DateRangeValidator.requireEndNotBeforeStart(req.startDate(), req.endDate());
    Vehicle vehicle =
        vehicleRepository
            .findByIdAndDeletedFalse(req.vehicleId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Vehicle not found: " + req.vehicleId()));
    BigDecimal draftBase =
        RentalCommissionFromVehicle.baseRentalCharge(
            req.startDate(), req.endDate(), vehicle.getRentalDailyPrice());
    var commissionSnap = RentalCommissionFromVehicle.deriveSnapshot(vehicle, draftBase);
    RentalCommissionFromVehicle.validateDerivedOrThrow(commissionSnap);
    RentalStatus status =
        (req.status() != null && !req.status().isBlank())
            ? parseRentalStatusRequired(req.status())
            : RentalStatus.active;
    List<Rental> sameVehicle =
        rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(req.vehicleId());
    ensureNoOverlap(sameVehicle, req.startDate(), req.endDate(), null);
    Rental rental = new Rental();
    rental.setVehicleId(req.vehicleId());
    rental.setUserId(req.userId());
    rental.setStartDate(req.startDate());
    rental.setEndDate(req.endDate());
    rental.setPickupHandoverLocation(
        resolvePickupHandover(vehicle, req.pickupHandoverLocationId()));
    rental.setReturnHandoverLocation(
        resolveReturnHandover(vehicle, req.returnHandoverLocationId()));
    RentalStatusDefinition statusDefinition = requireRentalStatusDefinition(status);
    rental.setRentalStatusId(statusDefinition.getId());
    rental.setStatusDefinition(statusDefinition);
    rental.setCommissionAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    rental.setCommissionFlow(RentalCommissionFlow.collect);
    rental.setCommissionCompany(null);
    Customer customer =
        customerRepository
            .findById(req.customerId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Customer not found: " + req.customerId()));
    customerRecordService.assertCustomerActive(customer);
    rental.setCustomer(customer);
    if (req.additionalDrivers() != null) {
      for (var d : req.additionalDrivers()) {
        RentalAdditionalDriver ad = new RentalAdditionalDriver();
        ad.setRental(rental);
        ad.setFullName(d.fullName().trim());
        ad.setBirthDate(d.birthDate());
        ad.setDriverLicenseNo(d.driverLicenseNo() != null ? d.driverLicenseNo().trim() : "");
        ad.setPassportNo(d.passportNo() != null ? d.passportNo().trim() : "");
        ad.setDriverLicenseImageDataUrl(d.driverLicenseImageDataUrl().trim());
        ad.setPassportImageDataUrl(d.passportImageDataUrl().trim());
        rental.getAdditionalDrivers().add(ad);
      }
    }
    replaceRentalOptions(
        rental, req.vehicleOptionDefinitionIds(), req.reservationExtraTemplateIds());
    rental = rentalRepository.save(rental);
    persistRentalMediaToObjectStorage(rental);
    rental.setNetAmount(computeNetAmount(rental, vehicle));
    rental = rentalRepository.save(rental);
    auditLog.infoEvent(
        "rental_created",
        Map.of(
            "rentalId", rental.getId().toString(),
            "vehicleId", Objects.toString(rental.getVehicleId(), ""),
            "status", rental.getStatus().name()));
    return RentalMapper.toDto(rental, objectStorageService::resolvePublicUrl);
  }

  @Transactional
  public RentalDto updateStatus(Long id, String statusRaw) {
    RentalStatus status = parseRentalStatusRequired(statusRaw);
    Rental rental =
        rentalRepository
            .findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rental not found: " + id));
    if (rental.getStatus() == status) {
      return RentalMapper.toDto(rental, objectStorageService::resolvePublicUrl);
    }
    if (status != RentalStatus.cancelled) {
      List<Rental> sameVehicle =
          rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(rental.getVehicleId());
      ensureNoOverlap(sameVehicle, rental.getStartDate(), rental.getEndDate(), rental.getId());
    }
    RentalStatusDefinition statusDefinition = requireRentalStatusDefinition(status);
    rental.setRentalStatusId(statusDefinition.getId());
    rental.setStatusDefinition(statusDefinition);
    Rental saved = rentalRepository.save(rental);
    syncDefaultPickupHandoverFromCompletedRental(saved);
    auditLog.infoEvent(
        "rental_status_updated",
        Map.of("rentalId", saved.getId().toString(), "status", status.name()));
    return RentalMapper.toDto(saved, objectStorageService::resolvePublicUrl);
  }

  @Transactional
  public RentalDto update(Long id, UpdateRentalRequest req) {
    Rental rental =
        rentalRepository
            .findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rental not found: " + id));
    LocalDate nextStart = req.startDate() != null ? req.startDate() : rental.getStartDate();
    LocalDate nextEnd = req.endDate() != null ? req.endDate() : rental.getEndDate();
    DateRangeValidator.requireEndNotBeforeStart(nextStart, nextEnd);

    RentalStatus nextStatus =
        (req.status() != null && !req.status().isBlank())
            ? parseRentalStatusRequired(req.status())
            : rental.getStatus();

    if (nextStatus != RentalStatus.cancelled) {
      List<Rental> sameVehicle =
          rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(rental.getVehicleId());
      ensureNoOverlap(sameVehicle, nextStart, nextEnd, rental.getId());
    }

    rental.setStartDate(nextStart);
    rental.setEndDate(nextEnd);
    if (req.pickupHandoverLocationId() != null) {
      rental.setPickupHandoverLocation(
          handoverLocationService.requireForAssignment(
              req.pickupHandoverLocationId(), HandoverLocationKind.PICKUP));
    }
    if (req.returnHandoverLocationId() != null) {
      Vehicle v = resolveRentalVehicle(rental);
      List<Long> allowedReturns = v.orderedReturnHandoverLocationIds();
      Long rid = req.returnHandoverLocationId();
      if (!allowedReturns.isEmpty() && !allowedReturns.contains(rid)) {
        throw new BadRequestException("Bu araç için seçilen teslim noktası geçerli değil.");
      }
      rental.setReturnHandoverLocation(
          handoverLocationService.requireForAssignment(rid, HandoverLocationKind.RETURN));
    }
    RentalStatusDefinition statusDefinition = requireRentalStatusDefinition(nextStatus);
    rental.setRentalStatusId(statusDefinition.getId());
    rental.setStatusDefinition(statusDefinition);
    rental.setCommissionAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    rental.setCommissionFlow(RentalCommissionFlow.collect);
    rental.setCommissionCompany(null);

    if (req.discountAmount() != null) {
      rental.setDiscountAmount(req.discountAmount().setScale(2, RoundingMode.HALF_UP));
    }
    if (req.discountType() != null) {
      rental.setDiscountType(
          req.discountType().isBlank() ? null : req.discountType().trim().toUpperCase());
    }

    if (req.customer() != null) {
      customerService.updateCustomer(rental.getCustomer(), req.customer(), rental.getId());
      customerRecordService.assertCustomerActive(rental.getCustomer());
    }

    if (req.vehicleOptionDefinitionIds() != null || req.reservationExtraTemplateIds() != null) {
      List<Long> vehicleIds =
          req.vehicleOptionDefinitionIds() != null
              ? req.vehicleOptionDefinitionIds()
              : existingVehicleOptionDefinitionIds(rental);
      List<Long> extraIds =
          req.reservationExtraTemplateIds() != null
              ? req.reservationExtraTemplateIds()
              : existingReservationExtraTemplateIds(rental);
      replaceRentalOptions(rental, vehicleIds, extraIds);
    }

    Vehicle commissionVehicle = resolveRentalVehicle(rental);
    BigDecimal amendedBase =
        RentalCommissionFromVehicle.baseRentalCharge(
            nextStart, nextEnd, commissionVehicle.getRentalDailyPrice());
    RentalCommissionFromVehicle.validateDerivedOrThrow(
        RentalCommissionFromVehicle.deriveSnapshot(commissionVehicle, amendedBase));

    rental.setNetAmount(computeNetAmount(rental, commissionVehicle));
    Rental saved = rentalRepository.save(rental);
    syncDefaultPickupHandoverFromCompletedRental(saved);
    return RentalMapper.toDto(saved, objectStorageService::resolvePublicUrl);
  }

  private List<Rental> findRentalsForListFilter(Long vehicleId, RentalStatus status) {
    if (vehicleId != null && status != null) {
      return rentalRepository.findByVehicle_IdAndStatusDefinition_CodeOrderByCreatedAtDesc(
          vehicleId, status.persistenceCode());
    }
    if (vehicleId != null) {
      return rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(vehicleId);
    }
    if (status != null) {
      return rentalRepository.findByStatusDefinition_CodeOrderByCreatedAtDesc(
          status.persistenceCode());
    }
    return rentalRepository.findAllByOrderByCreatedAtDesc();
  }

  private void syncDefaultPickupHandoverFromCompletedRental(Rental saved) {
    if (saved.getStatus() != RentalStatus.completed) {
      return;
    }
    Vehicle v = saved.getVehicle();
    if (v == null || saved.getReturnHandoverLocation() == null) {
      return;
    }
    v.setDefaultPickupHandoverLocation(saved.getReturnHandoverLocation());
    vehicleRepository.save(v);
  }

  private void persistRentalMediaToObjectStorage(Rental rental) {
    Customer c = rental.getCustomer();
    c.setPassportImageDataUrl(
        objectStorageService.uploadDataUrl(
            "rentals/" + rental.getId() + "/customer/passport",
            "passport",
            c.getPassportImageDataUrl()));
    c.setDriverLicenseImageDataUrl(
        objectStorageService.uploadDataUrl(
            "rentals/" + rental.getId() + "/customer/license",
            "license",
            c.getDriverLicenseImageDataUrl()));

    for (RentalAdditionalDriver driver : rental.getAdditionalDrivers()) {
      driver.setPassportImageDataUrl(
          objectStorageService.uploadDataUrl(
              "rentals/" + rental.getId() + "/drivers/" + driver.getId() + "/passport",
              "passport",
              driver.getPassportImageDataUrl()));
      driver.setDriverLicenseImageDataUrl(
          objectStorageService.uploadDataUrl(
              "rentals/" + rental.getId() + "/drivers/" + driver.getId() + "/license",
              "license",
              driver.getDriverLicenseImageDataUrl()));
    }
    customerRepository.save(c);
  }

  private static boolean datesOverlap(
      LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
    return !aStart.isAfter(bEnd) && !bStart.isAfter(aEnd);
  }

  private static boolean overlapsRange(
      LocalDate rentalStart, LocalDate rentalEnd, LocalDate filterStart, LocalDate filterEnd) {
    if (filterStart == null && filterEnd == null) {
      return true;
    }
    LocalDate start = filterStart != null ? filterStart : LocalDate.MIN;
    LocalDate end = filterEnd != null ? filterEnd : LocalDate.MAX;
    return datesOverlap(rentalStart, rentalEnd, start, end);
  }

  private void ensureNoOverlap(
      List<Rental> sameVehicle, LocalDate startDate, LocalDate endDate, Long skipRentalId) {
    for (Rental r : sameVehicle) {
      if (skipRentalId != null && skipRentalId.equals(r.getId())) {
        continue;
      }
      if (r.getStatus() == RentalStatus.cancelled) {
        continue;
      }
      if (datesOverlap(r.getStartDate(), r.getEndDate(), startDate, endDate)) {
        auditLog.warnBusiness(
            SafeReasonCodes.RENTAL_OVERLAP,
            Map.of(
                "blockingRentalId", r.getId().toString(),
                "vehicleId", Objects.toString(r.getVehicleId(), ""),
                "overlapStart", r.getStartDate().toString(),
                "overlapEnd", r.getEndDate().toString(),
                "requestedStart", startDate.toString(),
                "requestedEnd", endDate.toString()));
        String blockingCustomer =
            r.getCustomer() != null && r.getCustomer().getFullName() != null
                ? r.getCustomer().getFullName()
                : "?";
        throw new ConflictException(
            "Bu araç "
                + r.getStartDate()
                + " - "
                + r.getEndDate()
                + " aralığında kirada ("
                + blockingCustomer
                + ").");
      }
    }
  }

  private static List<Long> existingVehicleOptionDefinitionIds(Rental rental) {
    return rental.getOptions().stream()
        .map(RentalOption::getVehicleOptionDefinitionId)
        .filter(Objects::nonNull)
        .toList();
  }

  private static List<Long> existingReservationExtraTemplateIds(Rental rental) {
    return rental.getOptions().stream()
        .map(RentalOption::getReservationExtraTemplateId)
        .filter(Objects::nonNull)
        .toList();
  }

  private void replaceRentalOptions(
      Rental rental, List<Long> vehicleOptionDefinitionIds, List<Long> reservationExtraTemplateIds) {
    rental.getOptions().clear();
    boolean emptyVehicle =
        vehicleOptionDefinitionIds == null || vehicleOptionDefinitionIds.isEmpty();
    boolean emptyRental =
        reservationExtraTemplateIds == null || reservationExtraTemplateIds.isEmpty();
    if (emptyVehicle && emptyRental) {
      return;
    }
    Vehicle vehicle = resolveRentalVehicle(rental);
    int i = 0;
    if (vehicleOptionDefinitionIds != null) {
      for (Long definitionId : vehicleOptionDefinitionIds) {
        RentalOption row = mapVehicleOption(rental, vehicle, definitionId, i++);
        rental.getOptions().add(row);
      }
    }
    if (reservationExtraTemplateIds != null) {
      for (Long templateId : reservationExtraTemplateIds) {
        RentalOption row = mapRentalOption(rental, templateId, i++);
        rental.getOptions().add(row);
      }
    }
  }

  private RentalOption mapVehicleOption(
      Rental rental, Vehicle vehicle, Long vehicleOptionDefinitionId, int lineOrder) {
    var def =
        vehicleOptionDefinitionRepository
            .findByIdAndVehicle_Id(vehicleOptionDefinitionId, vehicle.getId())
            .orElseThrow(() -> new BadRequestException("Geçersiz araç seçeneği."));
    if (!def.isActive()) {
      throw new BadRequestException("Seçilen araç seçeneği artık kullanılamaz.");
    }
    RentalOption row = new RentalOption();
    row.setRental(rental);
    row.setVehicleOptionDefinitionId(def.getId());
    row.setLineOrder(lineOrder);
    return row;
  }

  private RentalOption mapRentalOption(Rental rental, Long reservationExtraTemplateId, int lineOrder) {
    var template =
        reservationExtraOptionTemplateRepository
            .findById(reservationExtraTemplateId)
            .orElseThrow(() -> new BadRequestException("Geçersiz rezervasyon ek seçeneği."));
    if (!template.isActive()) {
      throw new BadRequestException("Seçilen rezervasyon ek seçeneği artık kullanılamaz.");
    }
    RentalOption row = new RentalOption();
    row.setRental(rental);
    row.setReservationExtraTemplateId(template.getId());
    row.setLineOrder(lineOrder);
    return row;
  }

  private BigDecimal computeNetAmount(Rental rental, Vehicle vehicle) {
    long days = ChronoUnit.DAYS.between(rental.getStartDate(), rental.getEndDate()) + 1;
    BigDecimal dailyPrice =
        vehicle.getRentalDailyPrice() != null ? vehicle.getRentalDailyPrice() : BigDecimal.ZERO;
    BigDecimal base = dailyPrice.multiply(BigDecimal.valueOf(days));
    BigDecimal optionsSum =
        rental.getOptions().stream()
            .map(this::resolveOptionPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    var commissionSnap = RentalCommissionFromVehicle.deriveSnapshot(vehicle, base);
    BigDecimal commissionSigned =
        commissionSnap.flow() == RentalCommissionFlow.pay
            ? commissionSnap.amount().negate()
            : commissionSnap.amount();
    BigDecimal discountAmt =
        rental.getDiscountAmount() != null ? rental.getDiscountAmount() : BigDecimal.ZERO;
    BigDecimal discount;
    if ("PERCENT".equalsIgnoreCase(rental.getDiscountType())) {
      discount =
          base.multiply(discountAmt).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    } else {
      discount = discountAmt;
    }
    return base.add(optionsSum)
        .add(commissionSigned)
        .subtract(discount)
        .setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal resolveOptionPrice(RentalOption option) {
    if (option.getVehicleOptionDefinition() != null) {
      return option.getVehicleOptionDefinition().getPrice();
    }
    if (option.getReservationExtraTemplate() != null) {
      return option.getReservationExtraTemplate().getPrice();
    }
    if (option.getVehicleOptionDefinitionId() != null) {
      return vehicleOptionDefinitionRepository
          .findById(option.getVehicleOptionDefinitionId())
          .map(v -> v.getPrice())
          .orElse(BigDecimal.ZERO);
    }
    if (option.getReservationExtraTemplateId() != null) {
      return reservationExtraOptionTemplateRepository
          .findById(option.getReservationExtraTemplateId())
          .map(t -> t.getPrice())
          .orElse(BigDecimal.ZERO);
    }
    return BigDecimal.ZERO;
  }

  private Vehicle resolveRentalVehicle(Rental rental) {
    if (rental.getVehicle() != null) {
      return rental.getVehicle();
    }
    Long vid = rental.getVehicleId();
    if (vid == null) {
      throw new BadRequestException("Kiralama satırında araç kimliği eksik.");
    }
    return vehicleRepository
        .findByIdAndDeletedFalse(vid)
        .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vid));
  }

  private HandoverLocation resolvePickupHandover(Vehicle vehicle, Long requestPickupId) {
    Long pickupId = requestPickupId;
    if (pickupId == null && vehicle.getDefaultPickupHandoverLocation() != null) {
      pickupId = vehicle.getDefaultPickupHandoverLocation().getId();
    }
    if (pickupId == null) {
      return null;
    }
    return requestPickupId != null
        ? handoverLocationService.requireForAssignment(pickupId, HandoverLocationKind.PICKUP)
        : handoverLocationService.requireActive(pickupId);
  }

  private HandoverLocation resolveReturnHandover(Vehicle vehicle, Long requestReturnId) {
    List<Long> allowed = vehicle.orderedReturnHandoverLocationIds();
    boolean inferred = requestReturnId == null;
    Long returnId = requestReturnId;
    if (returnId == null && !allowed.isEmpty()) {
      returnId = allowed.get(0);
    }
    if (returnId == null) {
      return null;
    }
    if (!allowed.isEmpty() && !allowed.contains(returnId)) {
      throw new BadRequestException("Bu araç için seçilen teslim noktası geçerli değil.");
    }
    return inferred
        ? handoverLocationService.requireActive(returnId)
        : handoverLocationService.requireForAssignment(returnId, HandoverLocationKind.RETURN);
  }

  private RentalStatusDefinition requireRentalStatusDefinition(RentalStatus status) {
    for (String code : status.dbLookupCodes()) {
      var row = rentalStatusDefinitionRepository.findByCodeIgnoreCase(code);
      if (row.isPresent()) {
        return row.get();
      }
    }
    throw new BadRequestException(
        messageSource.getMessage(
            "rental.error.statusNotFound",
            new Object[] {status.persistenceCode()},
            LocaleContextHolder.getLocale()));
  }

  private RentalStatus parseRentalStatusRequired(String raw) {
    try {
      return RentalStatus.parseRequired(raw);
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException(
          messageSource.getMessage(
              "rental.error.invalidStatus", new Object[] {raw}, LocaleContextHolder.getLocale()));
    }
  }
}
