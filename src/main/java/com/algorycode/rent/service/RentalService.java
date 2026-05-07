package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CreateRentalRequest;
import com.algorycode.rent.api.dto.RentalDto;
import com.algorycode.rent.api.dto.RentalOptionRequest;
import com.algorycode.rent.api.dto.UpdateRentalRequest;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.api.mapper.RentalMapper;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.domain.location.HandoverLocationKind;
import com.algorycode.rent.domain.rental.CustomerSnapshot;
import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalAdditionalDriver;
import com.algorycode.rent.domain.rental.RentalCommissionFlow;
import com.algorycode.rent.domain.rental.RentalOption;
import com.algorycode.rent.domain.rental.RentalStatus;
import com.algorycode.rent.domain.rental.RentalStatusDefinition;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleStatus;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.logging.SafeReasonCodes;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.repository.RentalStatusDefinitionRepository;
import com.algorycode.rent.repository.ReservationExtraOptionTemplateRepository;
import com.algorycode.rent.repository.VehicleOptionDefinitionRepository;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.service.support.DateRangeValidator;
import com.algorycode.rent.service.support.RentalCommissionFromVehicle;
import com.algorycode.rent.service.support.RentalOptionLineResolution;
import com.algorycode.rent.service.support.Text;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
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
  private final ObjectStorageService objectStorageService;
  private final CustomerRecordService customerRecordService;
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
    if (vehicle.getStatus() == VehicleStatus.maintenance) {
      throw new ConflictException("Bakımdaki araç kiralanamaz.");
    }
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
    CustomerSnapshot c = new CustomerSnapshot();
    c.setFullName(req.customer().fullName().trim());
    c.setNationalId(req.customer().nationalId() != null ? req.customer().nationalId().trim() : "");
    c.setPassportNo(req.customer().passportNo() != null ? req.customer().passportNo().trim() : "");
    c.setPhone(req.customer().phone().trim());
    c.setEmail(req.customer().email() != null ? req.customer().email().trim() : null);
    c.setBirthDate(req.customer().birthDate());
    c.setDriverLicenseNo(
        req.customer().driverLicenseNo() != null ? req.customer().driverLicenseNo().trim() : null);
    c.setDriverLicenseImageDataUrl(
        req.customer().driverLicenseImageDataUrl() != null
            ? req.customer().driverLicenseImageDataUrl().trim()
            : null);
    c.setPassportImageDataUrl(
        req.customer().passportImageDataUrl() != null
            ? req.customer().passportImageDataUrl().trim()
            : null);
    rental.setCustomer(c);
    customerRecordService.assertCustomerActive(c);
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
    if (req.options() != null) {
      replaceRentalOptions(rental, req.options());
    }
    rental = rentalRepository.save(rental);
    persistRentalMediaToObjectStorage(rental);
    rental.setNetAmount(computeNetAmount(rental, vehicle));
    rental = rentalRepository.save(rental);
    auditLog.infoEvent(
        "rental_created",
        Map.of(
            "rentalId", rental.getId().toString(),
            "vehicleId", rental.getVehicle().getId().toString(),
            "status", rental.getStatus().name()));
    return RentalMapper.toDto(rental, objectStorageService::resolvePublicUrl);
  }

  @Transactional
  public RentalDto updateStatus(Long id, String statusRaw) {
    RentalStatus status = parseRentalStatusRequired(statusRaw);
    Rental rental =
        rentalRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rental not found: " + id));
    if (rental.getStatus() == status) {
      return RentalMapper.toDto(rental, objectStorageService::resolvePublicUrl);
    }
    if (status != RentalStatus.cancelled) {
      List<Rental> sameVehicle =
          rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(rental.getVehicle().getId());
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
            .findById(id)
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
          rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(rental.getVehicle().getId());
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
      Vehicle v = rental.getVehicle();
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
      CustomerSnapshot c = rental.getCustomer();
      if (req.customer().fullName() != null) c.setFullName(req.customer().fullName().trim());
      if (req.customer().nationalId() != null) c.setNationalId(req.customer().nationalId().trim());
      if (req.customer().passportNo() != null) c.setPassportNo(req.customer().passportNo().trim());
      if (req.customer().phone() != null) c.setPhone(req.customer().phone().trim());
      if (req.customer().email() != null) c.setEmail(Text.cleanOrNull(req.customer().email()));
      if (req.customer().birthDate() != null) c.setBirthDate(req.customer().birthDate());
      if (req.customer().driverLicenseNo() != null)
        c.setDriverLicenseNo(Text.cleanOrNull(req.customer().driverLicenseNo()));
      if (req.customer().passportImageDataUrl() != null
          && !req.customer().passportImageDataUrl().isBlank()) {
        c.setPassportImageDataUrl(
            objectStorageService.uploadDataUrl(
                "rentals/" + rental.getId() + "/customer/passport",
                "passport",
                req.customer().passportImageDataUrl().trim()));
      }
      if (req.customer().driverLicenseImageDataUrl() != null
          && !req.customer().driverLicenseImageDataUrl().isBlank()) {
        c.setDriverLicenseImageDataUrl(
            objectStorageService.uploadDataUrl(
                "rentals/" + rental.getId() + "/customer/license",
                "license",
                req.customer().driverLicenseImageDataUrl().trim()));
      }
      rental.setCustomer(c);
      customerRecordService.assertCustomerActive(c);
    }

    if (req.options() != null) {
      replaceRentalOptions(rental, req.options());
    }

    Vehicle commissionVehicle = rental.getVehicle();
    BigDecimal amendedBase =
        RentalCommissionFromVehicle.baseRentalCharge(
            nextStart, nextEnd, commissionVehicle.getRentalDailyPrice());
    RentalCommissionFromVehicle.validateDerivedOrThrow(
        RentalCommissionFromVehicle.deriveSnapshot(commissionVehicle, amendedBase));

    rental.setNetAmount(computeNetAmount(rental, rental.getVehicle()));
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
    CustomerSnapshot c = rental.getCustomer();
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
                "vehicleId", r.getVehicle().getId().toString(),
                "overlapStart", r.getStartDate().toString(),
                "overlapEnd", r.getEndDate().toString(),
                "requestedStart", startDate.toString(),
                "requestedEnd", endDate.toString()));
        throw new ConflictException(
            "Bu araç "
                + r.getStartDate()
                + " - "
                + r.getEndDate()
                + " aralığında kirada ("
                + r.getCustomer().getFullName()
                + ").");
      }
    }
  }

  private void replaceRentalOptions(Rental rental, List<RentalOptionRequest> options) {
    rental.getOptions().clear();
    if (options == null || options.isEmpty()) {
      return;
    }
    Vehicle vehicle = rental.getVehicle();
    int i = 0;
    for (RentalOptionRequest o : options) {
      RentalOptionLineResolution.Resolved resolved =
          RentalOptionLineResolution.resolve(
              vehicle,
              o,
              vehicleOptionDefinitionRepository,
              reservationExtraOptionTemplateRepository);
      RentalOption row = new RentalOption();
      row.setRental(rental);
      row.setTitle(resolved.title());
      row.setDescription(resolved.description());
      row.setPrice(resolved.price().setScale(2, RoundingMode.HALF_UP));
      row.setIcon(resolved.icon());
      row.setLineOrder(i++);
      rental.getOptions().add(row);
    }
  }

  private BigDecimal computeNetAmount(Rental rental, Vehicle vehicle) {
    long days = ChronoUnit.DAYS.between(rental.getStartDate(), rental.getEndDate()) + 1;
    BigDecimal dailyPrice =
        vehicle.getRentalDailyPrice() != null ? vehicle.getRentalDailyPrice() : BigDecimal.ZERO;
    BigDecimal base = dailyPrice.multiply(BigDecimal.valueOf(days));
    BigDecimal optionsSum =
        rental.getOptions().stream()
            .map(o -> o.getPrice() != null ? o.getPrice() : BigDecimal.ZERO)
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
