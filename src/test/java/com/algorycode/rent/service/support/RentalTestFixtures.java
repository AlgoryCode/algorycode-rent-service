package com.algorycode.rent.service.support;

import com.algorycode.rent.entity.Rental;
import com.algorycode.rent.entity.RentalStatus;

public final class RentalTestFixtures {

  private RentalTestFixtures() {}

  public static void attachRentalStatus(Rental rental, RentalStatus status) {
    rental.setRentalStatus(status);
  }
}
