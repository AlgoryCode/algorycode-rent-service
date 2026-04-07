package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.payment.PaymentLogStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentLogDto(
    UUID id,
    Instant createdAt,
    BigDecimal amountTry,
    PaymentLogStatus status,
    String method,
    String plate,
    UUID vehicleId,
    String customerName,
    String reference,
    String note) {}
