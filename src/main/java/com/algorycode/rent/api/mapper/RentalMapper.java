package com.algorycode.rent.api.mapper;

import com.algorycode.rent.api.dto.RentalDto;
import com.algorycode.rent.api.dto.RentalDto.AccidentReportDto;
import com.algorycode.rent.api.dto.RentalDto.AdditionalDriverDto;
import com.algorycode.rent.api.dto.RentalDto.CustomerDto;
import com.algorycode.rent.api.dto.RentalDto.FeedbackDto;
import com.algorycode.rent.api.dto.RentalDto.RentalPhotoDto;
import com.algorycode.rent.domain.rental.AccidentPhoto;
import com.algorycode.rent.domain.rental.AccidentReport;
import com.algorycode.rent.domain.rental.RentalAdditionalDriver;
import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalFeedback;
import com.algorycode.rent.domain.rental.RentalPhoto;

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
    return new RentalDto(
        r.getId(),
        r.getVehicle().getId(),
        r.getStartDate(),
        r.getEndDate(),
        r.getCreatedAt(),
        r.getStatus(),
        r.getCommissionAmount(),
        r.getCommissionFlow(),
        r.getCommissionCompany(),
        customer,
        additionalDrivers,
        fb,
        photos,
        accidents);
  }

  private static AdditionalDriverDto additionalDriverDto(RentalAdditionalDriver d, Function<String, String> assetResolver) {
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
    return new RentalPhotoDto(p.getId().toString(), assetResolver.apply(p.getUrl()), p.getCaption());
  }

  private static AccidentReportDto accidentDto(AccidentReport ar, Function<String, String> assetResolver) {
    List<RentalPhotoDto> ap =
        ar.getPhotos().stream()
            .map(p -> accidentPhotoDto(p, assetResolver))
            .toList();
    return new AccidentReportDto(ar.getId(), ar.getAt(), ar.getDescription(), ap);
  }

  private static RentalPhotoDto accidentPhotoDto(AccidentPhoto p, Function<String, String> assetResolver) {
    return new RentalPhotoDto(p.getId().toString(), assetResolver.apply(p.getUrl()), p.getCaption());
  }
}
