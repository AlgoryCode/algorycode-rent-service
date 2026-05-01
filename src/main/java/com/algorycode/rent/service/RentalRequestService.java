package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CreateRentalRequestFormRequest;
import com.algorycode.rent.api.dto.RentalOptionRequest;
import com.algorycode.rent.api.dto.RentalRequestDto;
import com.algorycode.rent.api.dto.UpdateRentalRequestStatusRequest;
import com.algorycode.rent.api.dto.ValidateCouponResponse;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.api.mapper.RentalRequestMapper;
import com.algorycode.rent.config.AppRentalRequestProperties;
import com.algorycode.rent.contract.RentalContractPdfService;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.domain.location.HandoverLocationKind;
import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.domain.request.RentalRequestAdditionalDriver;
import com.algorycode.rent.domain.request.RentalRequestCustomerSnapshot;
import com.algorycode.rent.domain.request.RentalRequestOption;
import com.algorycode.rent.domain.request.RentalRequestPricedLine;
import com.algorycode.rent.domain.request.RentalRequestPricedLineType;
import com.algorycode.rent.domain.request.RentalRequestStatus;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleStatus;
import com.algorycode.rent.events.RentalRequestCreatedMailEvent;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.repository.RentalRequestRepository;
import com.algorycode.rent.repository.ReservationExtraOptionTemplateRepository;
import com.algorycode.rent.repository.VehicleOptionDefinitionRepository;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.service.support.DateRangeValidator;
import com.algorycode.rent.service.support.RentalOptionLineResolution;
import com.algorycode.rent.service.support.RentalRequestPricedLineAssembler;
import com.algorycode.rent.service.support.Text;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RentalRequestService {

  /** PDF bytes + safe filename for {@code Content-Disposition} (sözleşme indirme). */
  public record ContractPdfAttachment(byte[] content, String filename) {}

  private static final DateTimeFormatter REF_DATE_FMT = DateTimeFormatter.BASIC_ISO_DATE;

  private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

  private final RentalRequestRepository rentalRequestRepository;
  private final VehicleRepository vehicleRepository;
  private final AppRentalRequestProperties rentalRequestProperties;
  private final RentalContractPdfService rentalContractPdfService;
  private final RentalRequestWhatsappContractService rentalRequestWhatsappContractService;
  private final ObjectStorageService objectStorageService;
  private final CustomerRecordService customerRecordService;
  private final HandoverLocationService handoverLocationService;
  private final HandoverPricingService handoverPricingService;
  private final VehicleOptionDefinitionRepository vehicleOptionDefinitionRepository;
  private final ReservationExtraOptionTemplateRepository reservationExtraOptionTemplateRepository;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final RentalRequestNotificationService rentalRequestNotificationService;
  private final AuditLog auditLog;
  private final RentalRequestPricedLineAssembler rentalRequestPricedLineAssembler;
  private final DiscountCouponService discountCouponService;

  public RentalRequestService(
      RentalRequestRepository rentalRequestRepository,
      VehicleRepository vehicleRepository,
      AppRentalRequestProperties rentalRequestProperties,
      RentalContractPdfService rentalContractPdfService,
      RentalRequestWhatsappContractService rentalRequestWhatsappContractService,
      ObjectStorageService objectStorageService,
      CustomerRecordService customerRecordService,
      HandoverLocationService handoverLocationService,
      HandoverPricingService handoverPricingService,
      VehicleOptionDefinitionRepository vehicleOptionDefinitionRepository,
      ReservationExtraOptionTemplateRepository reservationExtraOptionTemplateRepository,
      ApplicationEventPublisher applicationEventPublisher,
      RentalRequestNotificationService rentalRequestNotificationService,
      AuditLog auditLog,
      RentalRequestPricedLineAssembler rentalRequestPricedLineAssembler,
      DiscountCouponService discountCouponService) {
    this.rentalRequestRepository = rentalRequestRepository;
    this.vehicleRepository = vehicleRepository;
    this.rentalRequestProperties = rentalRequestProperties;
    this.rentalContractPdfService = rentalContractPdfService;
    this.rentalRequestWhatsappContractService = rentalRequestWhatsappContractService;
    this.objectStorageService = objectStorageService;
    this.customerRecordService = customerRecordService;
    this.handoverLocationService = handoverLocationService;
    this.handoverPricingService = handoverPricingService;
    this.vehicleOptionDefinitionRepository = vehicleOptionDefinitionRepository;
    this.reservationExtraOptionTemplateRepository = reservationExtraOptionTemplateRepository;
    this.applicationEventPublisher = applicationEventPublisher;
    this.rentalRequestNotificationService = rentalRequestNotificationService;
    this.auditLog = auditLog;
    this.rentalRequestPricedLineAssembler = rentalRequestPricedLineAssembler;
    this.discountCouponService = discountCouponService;
  }

  @Transactional
  public RentalRequestDto create(CreateRentalRequestFormRequest req) {
    DateRangeValidator.requireEndNotBeforeStart(req.startDate(), req.endDate());
    Vehicle vehicle = null;
    if (req.vehicleId() != null) {
      vehicle =
          vehicleRepository
              .findByIdAndDeletedFalse(req.vehicleId())
              .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + req.vehicleId()));
      if (vehicle.getStatus() == VehicleStatus.maintenance) {
        throw new ConflictException("Bakımdaki araç için talep oluşturulamaz.");
      }
      if (vehicle.getStatus() == VehicleStatus.rented) {
        throw new ConflictException("Kirada olan araç için talep oluşturulamaz.");
      }
    }

    RentalRequest entity = new RentalRequest();
    entity.setReferenceNo(generateReferenceNo());
    entity.setStatus(RentalRequestStatus.pending);
    entity.setVehicle(vehicle);
    entity.setUserId(req.userId());
    entity.setStartDate(req.startDate());
    entity.setEndDate(req.endDate());
    entity.setRentalNights(RentalRequestPricedLineAssembler.rentalNightsBetween(req.startDate(), req.endDate()));
    entity.setStartTime(req.startTime() != null ? req.startTime() : LocalTime.of(8, 0));
    entity.setReturnTime(req.returnTime() != null ? req.returnTime() : LocalTime.of(8, 0));
    entity.setPickupHandoverLocation(resolvePickupForRequest(vehicle, req.pickupHandoverLocationId()));
    entity.setReturnHandoverLocation(resolveReturnForRequest(vehicle, req.returnHandoverLocationId()));
    var handoverQuote =
        handoverPricingService.quoteForPersistedPair(
            entity.getPickupHandoverLocation(), entity.getReturnHandoverLocation());
    entity.setHandoverPickupLegEur(handoverQuote.pickupLegEur());
    entity.setHandoverReturnLegEur(handoverQuote.returnLegEur());
    entity.setHandoverRouteEur(handoverQuote.routeEur());
    entity.setHandoverTotalEur(handoverQuote.totalEur());
    entity.setOutsideCountryTravel(req.outsideCountryTravel());
    BigDecimal greenInsuranceFee = resolveGreenInsuranceFee(req.outsideCountryTravel());
    entity.setGreenInsuranceFee(greenInsuranceFee);
    entity.setNote(req.note() != null ? req.note().trim() : null);

    RentalRequestCustomerSnapshot c = new RentalRequestCustomerSnapshot();
    c.setFullName(req.customer().fullName().trim());
    c.setPhone(req.customer().phone().trim());
    c.setEmail(req.customer().email().trim().toLowerCase(Locale.ROOT));
    c.setBirthDate(req.customer().birthDate());
    c.setNationalId(req.customer().nationalId() != null ? req.customer().nationalId().trim() : null);
    c.setPassportNo(Text.blankToEmpty(req.customer().passportNo()));
    c.setDriverLicenseNo(Text.blankToEmpty(req.customer().driverLicenseNo()));
    c.setPassportImageDataUrl(req.customer().passportImageDataUrl().trim());
    c.setDriverLicenseImageDataUrl(req.customer().driverLicenseImageDataUrl().trim());
    entity.setCustomer(c);
    customerRecordService.assertCustomerActive(c);

    if (req.additionalDrivers() != null) {
      for (var d : req.additionalDrivers()) {
        RentalRequestAdditionalDriver ad = new RentalRequestAdditionalDriver();
        ad.setRentalRequest(entity);
        ad.setFullName(d.fullName().trim());
        ad.setBirthDate(d.birthDate());
        ad.setDriverLicenseNo(Text.blankToEmpty(d.driverLicenseNo()));
        ad.setPassportNo(Text.blankToEmpty(d.passportNo()));
        ad.setPassportImageDataUrl(d.passportImageDataUrl().trim());
        ad.setDriverLicenseImageDataUrl(d.driverLicenseImageDataUrl().trim());
        entity.getAdditionalDrivers().add(ad);
      }
    }

    if (req.options() != null) {
      replaceRequestOptions(entity, req.options());
    }

    rentalRequestPricedLineAssembler.attach(
        entity,
        vehicle,
        req,
        handoverQuote,
        entity.getRentalNights() != null ? entity.getRentalNights() : 0,
        greenInsuranceFee);

    if (req.couponCode() != null && !req.couponCode().isBlank()) {
      applyDiscountCoupon(entity, req.couponCode().trim());
    }

    entity = rentalRequestRepository.save(entity);
    persistRequestMediaToObjectStorage(entity);
    entity = rentalRequestRepository.save(entity);

    RentalRequest refreshed = requireById(entity.getId());
    applicationEventPublisher.publishEvent(new RentalRequestCreatedMailEvent(refreshed.getId()));
    auditLog.infoEvent(
        "rental_request_created",
        Map.of(
            "rentalRequestId", refreshed.getId().toString(),
            "referenceNo", refreshed.getReferenceNo(),
            "vehicleId",
                refreshed.getVehicle() != null ? refreshed.getVehicle().getId().toString() : "none",
            "userId", refreshed.getUserId() != null ? refreshed.getUserId().toString() : "none"));
    return RentalRequestMapper.toDto(refreshed, objectStorageService::resolvePublicUrl);
  }

  /**
   * Onaylanmış talep için sözleşme PDF'i üretir, object storage'a yükler ve (yapılandırmadaysa) WhatsApp
   * bildirimini tetikler. Başvuru oluşturma sırasında PDF üretilmez; görseller önce depoda saklanır.
   */
  @Transactional
  public RentalRequestDto generateContract(Long id) {
    RentalRequest entity = requireById(id);
    assertApprovedForContractGeneration(entity);
    if (entity.getContractPdfPath() != null && !entity.getContractPdfPath().isBlank()) {
      throw new BadRequestException("Bu talep için sözleşme zaten oluşturulmuş.");
    }
    String localPdfPath = rentalContractPdfService.generateFor(entity);
    entity.setContractPdfPath(uploadContractPdf(entity, localPdfPath));
    entity = rentalRequestRepository.save(entity);
    rentalRequestWhatsappContractService.notifyAdminWithContractPdf(entity);
    RentalRequest refreshed = requireById(entity.getId());
    return RentalRequestMapper.toDto(refreshed, objectStorageService::resolvePublicUrl);
  }

  @Transactional(readOnly = true)
  public RentalRequestDto getByReferenceNo(String referenceNo) {
    RentalRequest request =
        rentalRequestRepository
            .findByReferenceNoIgnoreCase(referenceNo.trim())
            .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + referenceNo));
    return RentalRequestMapper.toDto(request, objectStorageService::resolvePublicUrl);
  }

  @Transactional(readOnly = true)
  public RentalRequestDto getById(Long id) {
    RentalRequest request = requireById(id);
    return RentalRequestMapper.toDto(request, objectStorageService::resolvePublicUrl);
  }

  /**
   * Sözleşme PDF baytları (object key veya yerel dosya yolu). Sadece sözleşme oluşturulmuş taleplerde.
   */
  /**
   * Müşteri e-postasına Thymeleaf şablonlu sözleşme bildirimi (PDF herkese açık URL ise mail içinde link).
   */
  @Transactional(readOnly = true)
  public void queueContractPdfEmailToCustomer(Long id) {
    RentalRequest request = requireById(id);
    assertApprovedForContractEmail(request);
    assertContractPdfPathPresent(request, "Bu talep için sözleşme PDF'i henüz yok.");
    String email = request.getCustomer().getEmail();
    if (email == null || email.isBlank()) {
      throw new BadRequestException("Müşteri e-posta adresi kayıtlı değil.");
    }
    rentalRequestNotificationService.notifyContractPdfToCustomer(
        request, objectStorageService::resolvePublicUrl);
  }

  @Transactional(readOnly = true)
  public ContractPdfAttachment getContractPdfAttachment(Long id) {
    RentalRequest request = requireById(id);
    assertContractPdfPathPresent(request, "Sözleşme henüz oluşturulmamış.");
    String path = request.getContractPdfPath().trim();
    if (path.startsWith("http://") || path.startsWith("https://")) {
      throw new BadRequestException("Bu kayıt için doğrudan indirme desteklenmiyor.");
    }
    String filename =
        "sozlesme-"
            + request.getReferenceNo().replaceAll("[^A-Za-z0-9_.-]", "_")
            + ".pdf";
    try {
      Path local = Path.of(path);
      if (Files.isRegularFile(local)) {
        return new ContractPdfAttachment(Files.readAllBytes(local), filename);
      }
      if (objectStorageService.isActiveStorage()) {
        return new ContractPdfAttachment(objectStorageService.readObjectBytes(path), filename);
      }
    } catch (IOException e) {
      throw new BadRequestException("PDF okunamadı: " + e.getMessage());
    }
    throw new BadRequestException("Sözleşme dosyasına erişilemedi.");
  }

  @Transactional
  public RentalRequestDto updateStatus(Long id, UpdateRentalRequestStatusRequest req) {
    RentalRequest request = requireById(id);
    request.setStatus(req.status());
    request.setStatusMessage(req.statusMessage() != null ? req.statusMessage().trim() : null);
    request = rentalRequestRepository.save(request);
    return RentalRequestMapper.toDto(request, objectStorageService::resolvePublicUrl);
  }

  private RentalRequest requireById(Long id) {
    return rentalRequestRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + id));
  }

  private static void assertApprovedForContractGeneration(RentalRequest entity) {
    if (entity.getStatus() != RentalRequestStatus.approved) {
      throw new BadRequestException(
          "Sözleşme yalnızca onaylanmış (approved) talepler için oluşturulabilir.");
    }
  }

  private static void assertApprovedForContractEmail(RentalRequest request) {
    if (request.getStatus() != RentalRequestStatus.approved) {
      throw new BadRequestException("Sözleşme e-postası yalnızca onaylanmış talepler için gönderilebilir.");
    }
  }

  private static void assertContractPdfPathPresent(RentalRequest request, String messageWhenBlank) {
    if (request.getContractPdfPath() == null || request.getContractPdfPath().isBlank()) {
      throw new BadRequestException(messageWhenBlank);
    }
  }

  @Transactional(readOnly = true)
  public List<RentalRequestDto> listAll(Long vehicleId) {
    List<RentalRequest> rows =
        vehicleId == null
            ? rentalRequestRepository.findAll()
            : rentalRequestRepository.findByVehicle_IdOrderByCreatedAtDesc(vehicleId);
    return rows.stream()
        .map(r -> RentalRequestMapper.toDto(r, objectStorageService::resolvePublicUrl))
        .toList();
  }

  private void persistRequestMediaToObjectStorage(RentalRequest request) {
    RentalRequestCustomerSnapshot customer = request.getCustomer();
    customer.setPassportImageDataUrl(
        objectStorageService.uploadDataUrl(
            "rental-requests/" + request.getId() + "/customer/passport",
            "passport",
            customer.getPassportImageDataUrl()));
    customer.setDriverLicenseImageDataUrl(
        objectStorageService.uploadDataUrl(
            "rental-requests/" + request.getId() + "/customer/license",
            "license",
            customer.getDriverLicenseImageDataUrl()));

    for (RentalRequestAdditionalDriver driver : request.getAdditionalDrivers()) {
      driver.setPassportImageDataUrl(
          objectStorageService.uploadDataUrl(
              "rental-requests/" + request.getId() + "/drivers/" + driver.getId() + "/passport",
              "passport",
              driver.getPassportImageDataUrl()));
      driver.setDriverLicenseImageDataUrl(
          objectStorageService.uploadDataUrl(
              "rental-requests/" + request.getId() + "/drivers/" + driver.getId() + "/license",
              "license",
              driver.getDriverLicenseImageDataUrl()));
    }
  }

  private String uploadContractPdf(RentalRequest request, String localPdfPath) {
    if (!objectStorageService.isActiveStorage()) {
      return localPdfPath;
    }
    try {
      byte[] bytes = Files.readAllBytes(Path.of(localPdfPath));
      return objectStorageService.uploadBytes(
          "rental-requests/" + request.getId() + "/contracts",
          "contract_" + request.getReferenceNo(),
          "pdf",
          "application/pdf",
          bytes);
    } catch (Exception e) {
      return localPdfPath;
    }
  }

  private BigDecimal resolveGreenInsuranceFee(boolean outsideCountryTravel) {
    if (!outsideCountryTravel) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    return rentalRequestProperties.greenInsuranceFee().setScale(2, RoundingMode.HALF_UP);
  }

  private String generateReferenceNo() {
    String date = LocalDate.now().format(REF_DATE_FMT);
    for (int i = 0; i < 20; i++) {
      String ref = "RG-" + date + "-" + randomToken(6);
      if (!rentalRequestRepository.existsByReferenceNo(ref)) {
        return ref;
      }
    }
    return "RG-" + date + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
  }

  private static String randomToken(int len) {
    ThreadLocalRandom r = ThreadLocalRandom.current();
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      sb.append(ALPHABET.charAt(r.nextInt(ALPHABET.length())));
    }
    return sb.toString();
  }

  private void replaceRequestOptions(RentalRequest request, List<RentalOptionRequest> options) {
    request.getOptions().clear();
    if (options == null || options.isEmpty()) {
      return;
    }
    Vehicle vehicle = request.getVehicle();
    int i = 0;
    for (RentalOptionRequest o : options) {
      RentalOptionLineResolution.Resolved resolved = resolveRequestOptionLine(vehicle, o);
      RentalRequestOption row = new RentalRequestOption();
      row.setRentalRequest(request);
      row.setTitle(resolved.title());
      row.setDescription(resolved.description());
      row.setPrice(resolved.price().setScale(2, RoundingMode.HALF_UP));
      row.setIcon(resolved.icon());
      row.setLineOrder(i++);
      request.getOptions().add(row);
    }
  }

  private RentalOptionLineResolution.Resolved resolveRequestOptionLine(Vehicle vehicle, RentalOptionRequest o) {
    return RentalOptionLineResolution.resolve(
        vehicle, o, vehicleOptionDefinitionRepository, reservationExtraOptionTemplateRepository);
  }

  private void applyDiscountCoupon(RentalRequest entity, String code) {
    ValidateCouponResponse validation = discountCouponService.validate(code);
    if (!validation.valid()) {
      return;
    }
    BigDecimal total = entity.getPricingTotalTry() != null ? entity.getPricingTotalTry() : BigDecimal.ZERO;
    BigDecimal discountAmt;
    if ("PERCENT".equalsIgnoreCase(validation.discountType())) {
      discountAmt = total.multiply(validation.discountValue())
          .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    } else {
      discountAmt = validation.discountValue().setScale(2, RoundingMode.HALF_UP);
    }
    discountAmt = discountAmt.min(total);
    RentalRequestPricedLine discountLine = new RentalRequestPricedLine();
    discountLine.setRentalRequest(entity);
    discountLine.setLineType(RentalRequestPricedLineType.DISCOUNT);
    discountLine.setTitle("İndirim Kuponu: " + code.toUpperCase());
    discountLine.setDescription(validation.discountType() + " - " + validation.discountValue());
    discountLine.setLineOrder(entity.getPricedLines().size());
    discountLine.setQuantity(1);
    discountLine.setUnitAmount(discountAmt.negate());
    discountLine.setLineAmount(discountAmt.negate());
    discountLine.setCurrency("TRY");
    discountLine.setPricedAt(Instant.now());
    entity.getPricedLines().add(discountLine);
    entity.setPricingTotalTry(total.subtract(discountAmt).setScale(2, RoundingMode.HALF_UP));
    discountCouponService.incrementUsage(code);
  }

  private HandoverLocation resolvePickupForRequest(Vehicle vehicle, Long requestPickupId) {
    Long pickupId = requestPickupId;
    if (pickupId == null && vehicle != null && vehicle.getDefaultPickupHandoverLocation() != null) {
      pickupId = vehicle.getDefaultPickupHandoverLocation().getId();
    }
    if (pickupId == null) {
      return null;
    }
    return requestPickupId != null
        ? handoverLocationService.requireForAssignment(pickupId, HandoverLocationKind.PICKUP)
        : handoverLocationService.requireActive(pickupId);
  }

  private HandoverLocation resolveReturnForRequest(Vehicle vehicle, Long requestReturnId) {
    List<Long> allowed =
        vehicle != null ? vehicle.orderedReturnHandoverLocationIds() : List.of();
    boolean inferred = requestReturnId == null;
    Long returnId = requestReturnId;
    if (returnId == null && vehicle != null && !allowed.isEmpty()) {
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
}
