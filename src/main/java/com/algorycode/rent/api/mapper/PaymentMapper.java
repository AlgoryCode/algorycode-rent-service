package com.algorycode.rent.api.mapper;

import com.algorycode.rent.api.dto.PaymentLogDto;
import com.algorycode.rent.domain.payment.PaymentLog;
import com.algorycode.rent.domain.payment.PaymentMoneyFlow;
import com.algorycode.rent.domain.rental.Rental;

public final class PaymentMapper {

  private PaymentMapper() {}

  public static PaymentLogDto toDto(PaymentLog p) {
    Rental r = p.getRental();
    PaymentMoneyFlow flow = p.getMoneyFlow() != null ? p.getMoneyFlow() : PaymentMoneyFlow.inbound;
    return new PaymentLogDto(
        p.getId(),
        p.getCreatedAt(),
        p.getAmountTry(),
        flow,
        p.getStatus(),
        p.getMethod(),
        p.getPlate(),
        p.getVehicle() != null ? p.getVehicle().getId() : null,
        p.getCustomerName(),
        p.getReference(),
        p.getNote(),
        r != null ? r.getId() : null,
        r != null ? r.getStartDate() : null,
        r != null ? r.getEndDate() : null,
        r != null ? r.getStatus() : null,
        p.getRentalRevenueEur());
  }
}
