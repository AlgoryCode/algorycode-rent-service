package com.algorycode.rent.api.mapper;

import com.algorycode.rent.api.dto.PaymentLogDto;
import com.algorycode.rent.domain.payment.PaymentLog;

public final class PaymentMapper {

  private PaymentMapper() {}

  public static PaymentLogDto toDto(PaymentLog p) {
    return new PaymentLogDto(
        p.getId(),
        p.getCreatedAt(),
        p.getAmountTry(),
        p.getStatus(),
        p.getMethod(),
        p.getPlate(),
        p.getVehicle() != null ? p.getVehicle().getId() : null,
        p.getCustomerName(),
        p.getReference(),
        p.getNote());
  }
}
