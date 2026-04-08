package com.algorycode.rent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app.rental-request")
public record AppRentalRequestProperties(BigDecimal greenInsuranceFee) {

  public AppRentalRequestProperties {
    if (greenInsuranceFee == null) {
      greenInsuranceFee = BigDecimal.ZERO;
    }
  }
}
