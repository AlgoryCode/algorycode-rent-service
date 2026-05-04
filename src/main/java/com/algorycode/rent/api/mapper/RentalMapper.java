package com.algorycode.rent.api.mapper;

import com.algorycode.rent.api.dto.RentalDto;
import com.algorycode.rent.api.dto.RentalDto.AccidentReportDto;
import com.algorycode.rent.api.dto.RentalDto.AdditionalDriverDto;
import com.algorycode.rent.api.dto.RentalDto.CustomerDto;
import com.algorycode.rent.api.dto.RentalDto.FeedbackDto;
import com.algorycode.rent.api.dto.RentalDto.RentalOptionDto;
import com.algorycode.rent.api.dto.RentalDto.RentalPhotoDto;
import com.algorycode.rent.domain.rental.AccidentPhoto;
import com.algorycode.rent.domain.rental.AccidentReport;
import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalAdditionalDriver;
import com.algorycode.rent.domain.rental.RentalFeedback;
import com.algorycode.rent.domain.rental.RentalOption;
import com.algorycode.rent.domain.rental.RentalPhoto;
import com.algorycode.rent.service.support.RentalCommissionFromVehicle;
import java.util.List;
import java.util.function.Function;

public final class RentalMapper {

  private RentalMapper() {}

  public static RentalDto toDto(Rental r, Function<String, String> assetResolver) {
    var c = r.getCustomer();
    var customer =
        new CustomerDto(
            c.getFullName(),
            c.getNationalId(),
            c.getPassportNo(),
            c.getPhone(),
            c.getEmail(),
            c.getBirthDate(),
            c.getDriverLicenseNo(),
            assetResolver.apply(c.getDriverLicenseImageDataUrl()),
            assetResolver.apply(c.getPassportImageDataUrl()));
    FeedbackDto fb = null;
    RentalFeedback rf = r.getFeedback();
    if (rf != null) {
      fb = new FeedbackDto(rf.getAt(), rf.getText());
    }
    List<AdditionalDriverDto> additionalDrivers =
        r.getAdditionalDrivers().stream().map(d -> additionalDriverDto(d, assetResolver)).toList();
    List<RentalPhotoDto> photos =
        r.getPhotos().stream().map(p -> photoDto(p, assetResolver)).toList();
    List<AccidentReportDto> accidents =
        r.getAccidentReports().stream().map(a -> accidentDto(a, assetResolver)).toList();
    List<RentalOptionDto> options = r.getOptions().stream().map(RentalMapper::optionDto).toList();
    var commissionSnap =
        r.getVehicle() != null
            ? RentalCommissionFromVehicle.deriveSnapshot(r, r.getVehicle())
            : RentalCommissionFromVehicle.clearedSnapshot();
    return new RentalDto(
        r.getId(),
        r.getVehicleId(),
        r.getUserId(),
        r.getStartDate(),
        r.getEndDate(),
        HandoverLocationMapper.toRef(r.getPickupHandoverLocation()),
        HandoverLocationMapper.toRef(r.getReturnHandoverLocation()),
        r.getCreatedAt(),
        r.getStatus(),
        rentalStatusCode(r),
        commissionSnap.amount(),
        commissionSnap.flow(),
        commissionSnap.company(),
        r.getDiscountAmount(),
        r.getDiscountType(),
        r.getNetAmount(),
        customer,
        additionalDrivers,
        fb,
        photos,
        accidents,
        options);
  }

  private static String rentalStatusCode(Rental r) {
    if (r.getStatusDefinition() != null) {
      return r.getStatusDefinition().getCode();
    }
    return r.getStatus().name();
  }

  private static RentalOptionDto optionDto(RentalOption o) {
    return new RentalOptionDto(
        o.getId(), o.getTitle(), o.getDescription(), o.getPrice(), o.getIcon());
  }

  private static AdditionalDriverDto additionalDriverDto(
      RentalAdditionalDriver d, Function<String, String> assetResolver) {
    return new AdditionalDriverDto(
        d.getId(),
        d.getFullName(),
        d.getBirthDate(),
        d.getDriverLicenseNo(),
        d.getPassportNo(),
        assetResolver.apply(d.getDriverLicenseImageDataUrl()),
        assetResolver.apply(d.getPassportImageDataUrl()));
  }

  private static RentalPhotoDto photoDto(RentalPhoto p, Function<String, String> assetResolver) {
    return new RentalPhotoDto(p.getId(), assetResolver.apply(p.getUrl()), p.getCaption());
  }

  private static AccidentReportDto accidentDto(
      AccidentReport ar, Function<String, String> assetResolver) {
    List<RentalPhotoDto> ap =
        ar.getPhotos().stream().map(p -> accidentPhotoDto(p, assetResolver)).toList();
    return new AccidentReportDto(ar.getId(), ar.getAt(), ar.getDescription(), ap);
  }

  private static RentalPhotoDto accidentPhotoDto(
      AccidentPhoto p, Function<String, String> assetResolver) {
    return new RentalPhotoDto(p.getId(), assetResolver.apply(p.getUrl()), p.getCaption());
  }
}
