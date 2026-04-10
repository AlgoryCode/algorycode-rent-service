package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CreateRentalRequestFormRequest;
import com.algorycode.rent.api.dto.RentalRequestDto;
import com.algorycode.rent.api.dto.UpdateRentalRequestStatusRequest;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.api.mapper.RentalRequestMapper;
import com.algorycode.rent.config.AppRentalRequestProperties;
import com.algorycode.rent.contract.RentalContractPdfService;
import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.domain.request.RentalRequestAdditionalDriver;
import com.algorycode.rent.domain.request.RentalRequestCustomerSnapshot;
import com.algorycode.rent.domain.request.RentalRequestStatus;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.repository.RentalRequestRepository;
import com.algorycode.rent.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RentalRequestService {

  /** PDF bytes + safe filename for {@code Content-Disposition} (sözleşme indirme). */
  public record ContractPdfAttachment(byte[] content, String filename) {}

  private static final DateTimeFormatter REF_DATE_FMT = DateTimeFormatter.BASIC_ISO_DATE;

  private static String blankToEmpty(String s) {
    return s == null || s.isBlank() ? "" : s.trim();
  }
  private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

  private final RentalRequestRepository rentalRequestRepository;
  private final VehicleRepository vehicleRepository;
  private final AppRentalRequestProperties rentalRequestProperties;
  private final RentalContractPdfService rentalContractPdfService;
  private final RentalRequestWhatsappContractService rentalRequestWhatsappContractService;
  private final ObjectStorageService objectStorageService;

  public RentalRequestService(
      RentalRequestRepository rentalRequestRepository,
      VehicleRepository vehicleRepository,
      AppRentalRequestProperties rentalRequestProperties,
      RentalContractPdfService rentalContractPdfService,
      RentalRequestWhatsappContractService rentalRequestWhatsappContractService,
      ObjectStorageService objectStorageService) {
    this.rentalRequestRepository = rentalRequestRepository;
    this.vehicleRepository = vehicleRepository;
    this.rentalRequestProperties = rentalRequestProperties;
    this.rentalContractPdfService = rentalContractPdfService;
    this.rentalRequestWhatsappContractService = rentalRequestWhatsappContractService;
    this.objectStorageService = objectStorageService;
  }

  @Transactional
  public RentalRequestDto create(CreateRentalRequestFormRequest req) {
    if (req.endDate().isBefore(req.startDate())) {
      throw new BadRequestException("Bitiş tarihi başlangıçtan önce olamaz.");
    }
    Vehicle vehicle = null;
    if (req.vehicleId() != null) {
      vehicle =
          vehicleRepository
              .findById(req.vehicleId())
              .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + req.vehicleId()));
    }

    RentalRequest entity = new RentalRequest();
    entity.setReferenceNo(generateReferenceNo());
    entity.setStatus(RentalRequestStatus.pending);
    entity.setVehicle(vehicle);
    entity.setStartDate(req.startDate());
    entity.setEndDate(req.endDate());
    entity.setOutsideCountryTravel(req.outsideCountryTravel());
    entity.setGreenInsuranceFee(resolveGreenInsuranceFee(req.outsideCountryTravel()));
    entity.setNote(req.note() != null ? req.note().trim() : null);

    RentalRequestCustomerSnapshot c = new RentalRequestCustomerSnapshot();
    c.setFullName(req.customer().fullName().trim());
    c.setPhone(req.customer().phone().trim());
    c.setEmail(req.customer().email().trim().toLowerCase(Locale.ROOT));
    c.setBirthDate(req.customer().birthDate());
    c.setNationalId(req.customer().nationalId() != null ? req.customer().nationalId().trim() : null);
    c.setPassportNo(blankToEmpty(req.customer().passportNo()));
    c.setDriverLicenseNo(blankToEmpty(req.customer().driverLicenseNo()));
    c.setPassportImageDataUrl(req.customer().passportImageDataUrl().trim());
    c.setDriverLicenseImageDataUrl(req.customer().driverLicenseImageDataUrl().trim());
    entity.setCustomer(c);

    if (req.additionalDrivers() != null) {
      for (var d : req.additionalDrivers()) {
        RentalRequestAdditionalDriver ad = new RentalRequestAdditionalDriver();
        ad.setRentalRequest(entity);
        ad.setFullName(d.fullName().trim());
        ad.setBirthDate(d.birthDate());
        ad.setDriverLicenseNo(blankToEmpty(d.driverLicenseNo()));
        ad.setPassportNo(blankToEmpty(d.passportNo()));
        ad.setPassportImageDataUrl(d.passportImageDataUrl().trim());
        ad.setDriverLicenseImageDataUrl(d.driverLicenseImageDataUrl().trim());
        entity.getAdditionalDrivers().add(ad);
      }
    }

    entity = rentalRequestRepository.save(entity);
    persistRequestMediaToObjectStorage(entity);
    entity = rentalRequestRepository.save(entity);

    // E-posta kuyruğu geçici kapalı (SimpleMessageConverter + MailSendRequestedEvent uyumsuzluğu).
    RentalRequest refreshed =
        rentalRequestRepository.findById(entity.getId()).orElseThrow();
    return RentalRequestMapper.toDto(refreshed, objectStorageService::resolvePublicUrl);
  }

  /**
   * Onaylanmış talep için sözleşme PDF'i üretir, object storage'a yükler ve (yapılandırmadaysa) WhatsApp
   * bildirimini tetikler. Başvuru oluşturma sırasında PDF üretilmez; görseller önce depoda saklanır.
   */
  @Transactional
  public RentalRequestDto generateContract(UUID id) {
    RentalRequest entity =
        rentalRequestRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + id));
    if (entity.getStatus() != RentalRequestStatus.approved) {
      throw new BadRequestException(
          "Sözleşme yalnızca onaylanmış (approved) talepler için oluşturulabilir.");
    }
    if (entity.getContractPdfPath() != null && !entity.getContractPdfPath().isBlank()) {
      throw new BadRequestException("Bu talep için sözleşme zaten oluşturulmuş.");
    }
    String localPdfPath = rentalContractPdfService.generateFor(entity);
    entity.setContractPdfPath(uploadContractPdf(entity, localPdfPath));
    entity = rentalRequestRepository.save(entity);
    rentalRequestWhatsappContractService.notifyAdminWithContractPdf(entity);
    RentalRequest refreshed =
        rentalRequestRepository.findById(entity.getId()).orElseThrow();
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
  public RentalRequestDto getById(UUID id) {
    RentalRequest request =
        rentalRequestRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + id));
    return RentalRequestMapper.toDto(request, objectStorageService::resolvePublicUrl);
  }

  /**
   * Sözleşme PDF baytları (object key veya yerel dosya yolu). Sadece sözleşme oluşturulmuş taleplerde.
   */
  @Transactional(readOnly = true)
  public ContractPdfAttachment getContractPdfAttachment(UUID id) {
    RentalRequest request =
        rentalRequestRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + id));
    if (request.getContractPdfPath() == null || request.getContractPdfPath().isBlank()) {
      throw new BadRequestException("Sözleşme henüz oluşturulmamış.");
    }
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
  public RentalRequestDto updateStatus(UUID id, UpdateRentalRequestStatusRequest req) {
    RentalRequest request =
        rentalRequestRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + id));
    request.setStatus(req.status());
    request.setStatusMessage(req.statusMessage() != null ? req.statusMessage().trim() : null);
    request = rentalRequestRepository.save(request);
    return RentalRequestMapper.toDto(request, objectStorageService::resolvePublicUrl);
  }

  @Transactional(readOnly = true)
  public List<RentalRequestDto> listAll() {
    return rentalRequestRepository.findAll().stream()
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
}
