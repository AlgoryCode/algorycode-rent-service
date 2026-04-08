package com.algorycode.rent.api.mapper;

import com.algorycode.rent.api.dto.RentalRequestDto;
import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.domain.request.RentalRequestAdditionalDriver;

import java.util.List;
import java.util.UUID;

public final class RentalRequestMapper {

  private RentalRequestMapper() {}

  public static RentalRequestDto toDto(RentalRequest r) {
    var c = r.getCustomer();
    var customer =
        new RentalRequestDto.CustomerDto(
            c.getFullName(),
            c.getPhone(),
            c.getEmail(),
            c.getBirthDate(),
            c.getNationalId(),
            c.getPassportNo(),
            c.getDriverLicenseNo(),
            c.getPassportImageDataUrl(),
            c.getDriverLicenseImageDataUrl());

    List<RentalRequestDto.AdditionalDriverDto> additional =
        r.getAdditionalDrivers().stream().map(RentalRequestMapper::toAdditionalDriverDto).toList();

    UUID vehicleId = r.getVehicle() != null ? r.getVehicle().getId() : null;

    return new RentalRequestDto(
        r.getId(),
        r.getReferenceNo(),
        r.getCreatedAt(),
        r.getStatus(),
        r.getStatusMessage(),
        vehicleId,
        r.getStartDate(),
        r.getEndDate(),
        r.isOutsideCountryTravel(),
        r.getGreenInsuranceFee(),
        r.getNote(),
        r.getContractPdfPath(),
        r.getWhatsappContractSentAt(),
        r.getWhatsappContractError(),
        customer,
        additional);
  }

  private static RentalRequestDto.AdditionalDriverDto toAdditionalDriverDto(RentalRequestAdditionalDriver d) {
    return new RentalRequestDto.AdditionalDriverDto(
        d.getId(),
        d.getFullName(),
        d.getBirthDate(),
        d.getDriverLicenseNo(),
        d.getPassportNo(),
        d.getPassportImageDataUrl(),
        d.getDriverLicenseImageDataUrl());
  }
}
