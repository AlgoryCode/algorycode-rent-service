package com.algorycode.rent.mapper;

import com.algorycode.rent.dto.PaymentLogDto;
import com.algorycode.rent.entity.PaymentLog;
import com.algorycode.rent.entity.PaymentMoneyFlow;
import com.algorycode.rent.entity.Rental;

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
        p.getVehicleId(),
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
