package com.algorycode.rent.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rental-request")
public record AppRentalRequestProperties(BigDecimal greenInsuranceFee, BigDecimal tryPerEur) {

  public AppRentalRequestProperties {
    if (greenInsuranceFee == null) {
      greenInsuranceFee = BigDecimal.ZERO;
    }
    if (tryPerEur == null || tryPerEur.compareTo(BigDecimal.ZERO) <= 0) {
      tryPerEur = new BigDecimal("36.20");
    }
  }
}
