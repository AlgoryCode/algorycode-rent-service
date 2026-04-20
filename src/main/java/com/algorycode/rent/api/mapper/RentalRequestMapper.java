package com.algorycode.rent.api.mapper;

import com.algorycode.rent.api.dto.RentalRequestDto;
import com.algorycode.rent.api.dto.RentalRequestDto.RentalRequestOptionDto;
import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.domain.request.RentalRequestAdditionalDriver;
import com.algorycode.rent.domain.request.RentalRequestOption;
import com.algorycode.rent.domain.request.RentalRequestStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

public final class RentalRequestMapper {

  private RentalRequestMapper() {}

  public static RentalRequestDto toDto(RentalRequest r, Function<String, String> assetResolver) {
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
            assetResolver.apply(c.getPassportImageDataUrl()),
            assetResolver.apply(c.getDriverLicenseImageDataUrl()));

    List<RentalRequestDto.AdditionalDriverDto> additional =
        r.getAdditionalDrivers().stream().map(d -> toAdditionalDriverDto(d, assetResolver)).toList();

    List<RentalRequestOptionDto> options =
        r.getOptions().stream().map(RentalRequestMapper::requestOptionDto).toList();

    Long vehicleId = r.getVehicleId();

    boolean contractGenerationAvailable =
        r.getStatus() == RentalRequestStatus.approved
            && (r.getContractPdfPath() == null || r.getContractPdfPath().isBlank());

    return new RentalRequestDto(
        r.getId(),
        r.getReferenceNo(),
        r.getCreatedAt(),
        r.getStatus(),
        r.getStatusMessage(),
        vehicleId,
        r.getUserId(),
        r.getStartDate(),
        r.getEndDate(),
        r.getStartTime(),
        r.getReturnTime(),
        HandoverLocationMapper.toRef(r.getPickupHandoverLocation()),
        HandoverLocationMapper.toRef(r.getReturnHandoverLocation()),
        r.isOutsideCountryTravel(),
        r.getGreenInsuranceFee(),
        r.getNote(),
        assetResolver.apply(r.getContractPdfPath()),
        r.getWhatsappContractSentAt(),
        r.getWhatsappContractError(),
        contractGenerationAvailable,
        nz(r.getHandoverPickupLegEur()),
        nz(r.getHandoverReturnLegEur()),
        nz(r.getHandoverRouteEur()),
        nz(r.getHandoverTotalEur()),
        customer,
        additional,
        options);
  }

  private static BigDecimal nz(BigDecimal v) {
    return v != null ? v : BigDecimal.ZERO;
  }

  private static RentalRequestOptionDto requestOptionDto(RentalRequestOption o) {
    return new RentalRequestOptionDto(
        o.getId(), o.getTitle(), o.getDescription(), o.getPrice(), o.getIcon());
  }

  private static RentalRequestDto.AdditionalDriverDto toAdditionalDriverDto(
      RentalRequestAdditionalDriver d, Function<String, String> assetResolver) {
    return new RentalRequestDto.AdditionalDriverDto(
        d.getId(),
        d.getFullName(),
        d.getBirthDate(),
        d.getDriverLicenseNo(),
        d.getPassportNo(),
        assetResolver.apply(d.getPassportImageDataUrl()),
        assetResolver.apply(d.getDriverLicenseImageDataUrl()));
  }
}
