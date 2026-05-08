package com.algorycode.rent.mapper;

import com.algorycode.rent.dto.CustomerRequest;
import com.algorycode.rent.dto.CustomerResponse;
import com.algorycode.rent.entity.Customer;
import com.algorycode.rent.service.support.Text;
import java.util.function.Function;

public final class CustomerMapper {

  private CustomerMapper() {}

  public static CustomerResponse toResponse(Customer c, Function<String, String> assetResolver) {
    return new CustomerResponse(
        c.getId(),
        c.getCreatedAt(),
        c.getUpdatedAt(),
        c.getFullName(),
        c.getNationalId(),
        c.getPassportNo(),
        c.getPhone(),
        c.getEmail(),
        c.getBirthDate(),
        c.getDriverLicenseNo(),
        assetResolver.apply(c.getDriverLicenseImageDataUrl()),
        assetResolver.apply(c.getPassportImageDataUrl()));
  }

  public static void applyCreate(Customer c, CustomerRequest req) {
    c.setFullName(req.fullName().trim());
    c.setNationalId(req.nationalId() != null ? req.nationalId().trim() : "");
    c.setPassportNo(req.passportNo() != null ? req.passportNo().trim() : "");
    c.setPhone(req.phone().trim());
    c.setEmail(req.email() != null ? req.email().trim() : null);
    c.setBirthDate(req.birthDate());
    c.setDriverLicenseNo(
        req.driverLicenseNo() != null ? req.driverLicenseNo().trim() : null);
    c.setDriverLicenseImageDataUrl(
        req.driverLicenseImageDataUrl() != null
            ? req.driverLicenseImageDataUrl().trim()
            : null);
    c.setPassportImageDataUrl(
        req.passportImageDataUrl() != null ? req.passportImageDataUrl().trim() : null);
  }

  public static void mergeScalars(Customer c, CustomerRequest req) {
    if (req.fullName() != null) {
      c.setFullName(req.fullName().trim());
    }
    if (req.nationalId() != null) {
      c.setNationalId(req.nationalId().trim());
    }
    if (req.passportNo() != null) {
      c.setPassportNo(req.passportNo().trim());
    }
    if (req.phone() != null) {
      c.setPhone(req.phone().trim());
    }
    if (req.email() != null) {
      c.setEmail(Text.cleanOrNull(req.email()));
    }
    if (req.birthDate() != null) {
      c.setBirthDate(req.birthDate());
    }
    if (req.driverLicenseNo() != null) {
      c.setDriverLicenseNo(Text.cleanOrNull(req.driverLicenseNo()));
    }
  }
}
