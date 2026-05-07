package com.algorycode.rent.service.support;

import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalStatus;
import com.algorycode.rent.domain.rental.RentalStatusDefinition;

public final class RentalTestFixtures {

  private RentalTestFixtures() {}

  public static RentalStatusDefinition rentalStatusDefinition(String code) {
    var d = new RentalStatusDefinition();
    d.setCode(code);
    d.setLabelTr("");
    d.setSortOrder(0);
    return d;
  }

  public static void attachRentalStatus(Rental rental, RentalStatus status) {
    RentalStatusDefinition d = rentalStatusDefinition(status.name());
    d.setId((long) status.ordinal() + 900L);
    rental.setRentalStatusId(d.getId());
    rental.setStatusDefinition(d);
  }
}
