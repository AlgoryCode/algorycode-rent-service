package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.payment.PaymentLogStatus;
import com.algorycode.rent.domain.payment.PaymentMoneyFlow;
import com.algorycode.rent.domain.rental.RentalStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PaymentLogDto(
    Long id,
    Instant createdAt,
    BigDecimal amountTry,
    PaymentMoneyFlow moneyFlow,
    PaymentLogStatus status,
    String method,
    String plate,
    Long vehicleId,
    String customerName,
    String reference,
    String note,
    Long rentalId,
    LocalDate rentalStartDate,
    LocalDate rentalEndDate,
    RentalStatus rentalStatus,
    BigDecimal rentalRevenueEur) {}
