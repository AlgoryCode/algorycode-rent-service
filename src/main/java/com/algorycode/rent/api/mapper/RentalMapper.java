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

public final class RentalMapper {

  private RentalMapper() {}

  public static RentalDto toDto(Rental r) {
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
            c.getDriverLicenseImageDataUrl(),
            c.getPassportImageDataUrl());
    FeedbackDto fb = null;
    RentalFeedback rf = r.getFeedback();
    if (rf != null) {
      fb = new FeedbackDto(rf.getAt(), rf.getText());
    }
    List<AdditionalDriverDto> additionalDrivers =
        r.getAdditionalDrivers().stream().map(RentalMapper::additionalDriverDto).toList();
    List<RentalPhotoDto> photos =
        r.getPhotos().stream().map(RentalMapper::photoDto).toList();
    List<AccidentReportDto> accidents =
        r.getAccidentReports().stream().map(RentalMapper::accidentDto).toList();
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

  private static AdditionalDriverDto additionalDriverDto(RentalAdditionalDriver d) {
    return new AdditionalDriverDto(
        d.getId(),
        d.getFullName(),
        d.getBirthDate(),
        d.getDriverLicenseNo(),
        d.getPassportNo(),
        d.getDriverLicenseImageDataUrl(),
        d.getPassportImageDataUrl());
  }

  private static RentalPhotoDto photoDto(RentalPhoto p) {
    return new RentalPhotoDto(p.getId().toString(), p.getUrl(), p.getCaption());
  }

  private static AccidentReportDto accidentDto(AccidentReport ar) {
    List<RentalPhotoDto> ap =
        ar.getPhotos().stream()
            .map(RentalMapper::accidentPhotoDto)
            .toList();
    return new AccidentReportDto(ar.getId(), ar.getAt(), ar.getDescription(), ap);
  }

  private static RentalPhotoDto accidentPhotoDto(AccidentPhoto p) {
    return new RentalPhotoDto(p.getId().toString(), p.getUrl(), p.getCaption());
  }
}
