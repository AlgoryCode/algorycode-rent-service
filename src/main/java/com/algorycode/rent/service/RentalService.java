package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CreateRentalRequest;
import com.algorycode.rent.api.dto.RentalDto;
import com.algorycode.rent.api.dto.UpdateRentalRequest;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.api.mapper.RentalMapper;
import com.algorycode.rent.domain.rental.RentalAdditionalDriver;
import com.algorycode.rent.domain.rental.CustomerSnapshot;
import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalCommissionFlow;
import com.algorycode.rent.domain.rental.RentalStatus;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class RentalService {

  private final RentalRepository rentalRepository;
  private final VehicleRepository vehicleRepository;
  private final ObjectStorageService objectStorageService;

  public RentalService(
      RentalRepository rentalRepository,
      VehicleRepository vehicleRepository,
      ObjectStorageService objectStorageService) {
    this.rentalRepository = rentalRepository;
    this.vehicleRepository = vehicleRepository;
    this.objectStorageService = objectStorageService;
  }

  @Transactional(readOnly = true)
  public List<RentalDto> list(UUID vehicleId, RentalStatus status, LocalDate startDate, LocalDate endDate) {
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      throw new BadRequestException("Bitiş tarihi başlangıçtan önce olamaz.");
    }

    List<Rental> base;
    if (vehicleId != null && status != null) {
      base = rentalRepository.findByVehicle_IdAndStatusOrderByCreatedAtDesc(vehicleId, status);
    } else if (vehicleId != null) {
      base = rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(vehicleId);
    } else if (status != null) {
      base = rentalRepository.findByStatusOrderByCreatedAtDesc(status);
    } else {
      base = rentalRepository.findAllByOrderByCreatedAtDesc();
    }

    return base.stream()
        .filter(
            r ->
                overlapsRange(
                    r.getStartDate(),
                    r.getEndDate(),
                    startDate,
                    endDate))
        .map(r -> RentalMapper.toDto(r, objectStorageService::resolvePublicUrl))
        .toList();
  }

  @Transactional(readOnly = true)
  public RentalDto getById(UUID id) {
    var r =
        rentalRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rental not found: " + id));
    return RentalMapper.toDto(r, objectStorageService::resolvePublicUrl);
  }

  @Transactional
  public RentalDto create(CreateRentalRequest req) {
    if (req.endDate().isBefore(req.startDate())) {
      throw new BadRequestException("Bitiş tarihi başlangıçtan önce olamaz.");
    }
    Vehicle vehicle =
        vehicleRepository
            .findById(req.vehicleId())
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + req.vehicleId()));
    if (vehicle.isMaintenance()) {
      throw new ConflictException("Bakımdaki araç kiralanamaz.");
    }
    if (req.commissionAmount().compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException("Komisyon tutarı negatif olamaz.");
    }
    if (req.commissionFlow() == RentalCommissionFlow.pay
        && req.commissionAmount().compareTo(BigDecimal.ZERO) > 0
        && (req.commissionCompany() == null || req.commissionCompany().isBlank())) {
      throw new BadRequestException("Komisyon ödemesinde firma adı zorunludur.");
    }
    RentalStatus status = req.status() != null ? req.status() : RentalStatus.active;
    List<Rental> sameVehicle = rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(req.vehicleId());
    ensureNoOverlap(sameVehicle, req.startDate(), req.endDate(), null);
    Rental rental = new Rental();
    rental.setVehicle(vehicle);
    rental.setStartDate(req.startDate());
    rental.setEndDate(req.endDate());
    rental.setStatus(status);
    rental.setCommissionAmount(req.commissionAmount().setScale(2, RoundingMode.HALF_UP));
    rental.setCommissionFlow(req.commissionFlow());
    rental.setCommissionCompany(
        req.commissionCompany() != null && !req.commissionCompany().isBlank()
            ? req.commissionCompany().trim()
            : null);
    CustomerSnapshot c = new CustomerSnapshot();
    c.setFullName(req.customer().fullName().trim());
    c.setNationalId(req.customer().nationalId() != null ? req.customer().nationalId().trim() : "");
    c.setPassportNo(
        req.customer().passportNo() != null ? req.customer().passportNo().trim() : "");
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
    if (req.additionalDrivers() != null) {
      for (var d : req.additionalDrivers()) {
        RentalAdditionalDriver ad = new RentalAdditionalDriver();
        ad.setRental(rental);
        ad.setFullName(d.fullName().trim());
        ad.setBirthDate(d.birthDate());
        ad.setDriverLicenseNo(
            d.driverLicenseNo() != null ? d.driverLicenseNo().trim() : "");
        ad.setPassportNo(d.passportNo() != null ? d.passportNo().trim() : "");
        ad.setDriverLicenseImageDataUrl(d.driverLicenseImageDataUrl().trim());
        ad.setPassportImageDataUrl(d.passportImageDataUrl().trim());
        rental.getAdditionalDrivers().add(ad);
      }
    }
    rental = rentalRepository.save(rental);
    persistRentalMediaToObjectStorage(rental);
    rental = rentalRepository.save(rental);
    return RentalMapper.toDto(rental, objectStorageService::resolvePublicUrl);
  }

  @Transactional
  public RentalDto update(UUID id, UpdateRentalRequest req) {
    Rental rental =
        rentalRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rental not found: " + id));

    LocalDate nextStart = req.startDate() != null ? req.startDate() : rental.getStartDate();
    LocalDate nextEnd = req.endDate() != null ? req.endDate() : rental.getEndDate();
    if (nextEnd.isBefore(nextStart)) {
      throw new BadRequestException("Bitiş tarihi başlangıçtan önce olamaz.");
    }

    RentalStatus nextStatus = req.status() != null ? req.status() : rental.getStatus();

    BigDecimal nextCommission =
        req.commissionAmount() != null ? req.commissionAmount() : rental.getCommissionAmount();
    if (nextCommission.compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException("Komisyon tutarı negatif olamaz.");
    }
    nextCommission = nextCommission.setScale(2, RoundingMode.HALF_UP);

    RentalCommissionFlow nextFlow =
        req.commissionFlow() != null ? req.commissionFlow() : rental.getCommissionFlow();
    String nextCompany =
        req.commissionCompany() != null ? cleanOrNull(req.commissionCompany()) : rental.getCommissionCompany();
    if (nextFlow == RentalCommissionFlow.pay
        && nextCommission.compareTo(BigDecimal.ZERO) > 0
        && (nextCompany == null || nextCompany.isBlank())) {
      throw new BadRequestException("Komisyon ödemesinde firma adı zorunludur.");
    }

    if (nextStatus != RentalStatus.cancelled) {
      List<Rental> sameVehicle = rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(rental.getVehicle().getId());
      ensureNoOverlap(sameVehicle, nextStart, nextEnd, rental.getId());
    }

    rental.setStartDate(nextStart);
    rental.setEndDate(nextEnd);
    rental.setStatus(nextStatus);
    rental.setCommissionAmount(nextCommission);
    rental.setCommissionFlow(nextFlow);
    rental.setCommissionCompany(nextCompany);

    if (req.customer() != null) {
      CustomerSnapshot c = rental.getCustomer();
      if (req.customer().fullName() != null) c.setFullName(req.customer().fullName().trim());
      if (req.customer().nationalId() != null) c.setNationalId(req.customer().nationalId().trim());
      if (req.customer().passportNo() != null) c.setPassportNo(req.customer().passportNo().trim());
      if (req.customer().phone() != null) c.setPhone(req.customer().phone().trim());
      if (req.customer().email() != null) c.setEmail(cleanOrNull(req.customer().email()));
      if (req.customer().birthDate() != null) c.setBirthDate(req.customer().birthDate());
      if (req.customer().driverLicenseNo() != null) c.setDriverLicenseNo(cleanOrNull(req.customer().driverLicenseNo()));
      if (req.customer().passportImageDataUrl() != null && !req.customer().passportImageDataUrl().isBlank()) {
        c.setPassportImageDataUrl(
            objectStorageService.uploadDataUrl(
                "rentals/" + rental.getId() + "/customer/passport",
                "passport",
                req.customer().passportImageDataUrl().trim()));
      }
      if (req.customer().driverLicenseImageDataUrl() != null && !req.customer().driverLicenseImageDataUrl().isBlank()) {
        c.setDriverLicenseImageDataUrl(
            objectStorageService.uploadDataUrl(
                "rentals/" + rental.getId() + "/customer/license",
                "license",
                req.customer().driverLicenseImageDataUrl().trim()));
      }
      rental.setCustomer(c);
    }

    return RentalMapper.toDto(rentalRepository.save(rental), objectStorageService::resolvePublicUrl);
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

  private static boolean datesOverlap(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
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

  private void ensureNoOverlap(List<Rental> sameVehicle, LocalDate startDate, LocalDate endDate, UUID skipRentalId) {
    for (Rental r : sameVehicle) {
      if (skipRentalId != null && skipRentalId.equals(r.getId())) {
        continue;
      }
      if (r.getStatus() == RentalStatus.cancelled) {
        continue;
      }
      if (datesOverlap(r.getStartDate(), r.getEndDate(), startDate, endDate)) {
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

  private static String cleanOrNull(String input) {
    if (input == null) return null;
    String s = input.trim();
    return s.isBlank() ? null : s;
  }
}
