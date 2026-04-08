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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RentalRequestService {

  private static final DateTimeFormatter REF_DATE_FMT = DateTimeFormatter.BASIC_ISO_DATE;
  private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

  private final RentalRequestRepository rentalRequestRepository;
  private final VehicleRepository vehicleRepository;
  private final AppRentalRequestProperties rentalRequestProperties;
  private final RentalContractPdfService rentalContractPdfService;
  private final RentalRequestNotificationService rentalRequestNotificationService;
  private final RentalRequestWhatsappContractService rentalRequestWhatsappContractService;

  public RentalRequestService(
      RentalRequestRepository rentalRequestRepository,
      VehicleRepository vehicleRepository,
      AppRentalRequestProperties rentalRequestProperties,
      RentalContractPdfService rentalContractPdfService,
      RentalRequestNotificationService rentalRequestNotificationService,
      RentalRequestWhatsappContractService rentalRequestWhatsappContractService) {
    this.rentalRequestRepository = rentalRequestRepository;
    this.vehicleRepository = vehicleRepository;
    this.rentalRequestProperties = rentalRequestProperties;
    this.rentalContractPdfService = rentalContractPdfService;
    this.rentalRequestNotificationService = rentalRequestNotificationService;
    this.rentalRequestWhatsappContractService = rentalRequestWhatsappContractService;
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
    c.setPassportNo(req.customer().passportNo().trim());
    c.setDriverLicenseNo(req.customer().driverLicenseNo().trim());
    c.setPassportImageDataUrl(req.customer().passportImageDataUrl().trim());
    c.setDriverLicenseImageDataUrl(req.customer().driverLicenseImageDataUrl().trim());
    entity.setCustomer(c);

    if (req.additionalDrivers() != null) {
      for (var d : req.additionalDrivers()) {
        RentalRequestAdditionalDriver ad = new RentalRequestAdditionalDriver();
        ad.setRentalRequest(entity);
        ad.setFullName(d.fullName().trim());
        ad.setBirthDate(d.birthDate());
        ad.setDriverLicenseNo(d.driverLicenseNo().trim());
        ad.setPassportNo(d.passportNo().trim());
        ad.setPassportImageDataUrl(d.passportImageDataUrl().trim());
        ad.setDriverLicenseImageDataUrl(d.driverLicenseImageDataUrl().trim());
        entity.getAdditionalDrivers().add(ad);
      }
    }

    entity = rentalRequestRepository.save(entity);

    String pdfPath = rentalContractPdfService.generateFor(entity);
    entity.setContractPdfPath(pdfPath);
    entity = rentalRequestRepository.save(entity);

    rentalRequestNotificationService.notifyCreated(entity);
    rentalRequestWhatsappContractService.notifyAdminWithContractPdf(entity);
    RentalRequest refreshed =
        rentalRequestRepository.findById(entity.getId()).orElseThrow();
    return RentalRequestMapper.toDto(refreshed);
  }

  @Transactional(readOnly = true)
  public RentalRequestDto getByReferenceNo(String referenceNo) {
    RentalRequest request =
        rentalRequestRepository
            .findByReferenceNoIgnoreCase(referenceNo.trim())
            .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + referenceNo));
    return RentalRequestMapper.toDto(request);
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
    rentalRequestNotificationService.notifyStatusChanged(request);
    return RentalRequestMapper.toDto(request);
  }

  @Transactional(readOnly = true)
  public List<RentalRequestDto> listAll() {
    return rentalRequestRepository.findAll().stream().map(RentalRequestMapper::toDto).toList();
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
