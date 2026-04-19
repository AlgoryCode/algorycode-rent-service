package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.payment.PaymentLogStatus;
import com.algorycode.rent.domain.payment.PaymentMoneyFlow;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentLogRequest(
    @NotNull UUID rentalId,
    @NotNull @Positive BigDecimal amountTry,
    @NotBlank @Size(max = 128) String method,
    @NotNull PaymentMoneyFlow moneyFlow,
    PaymentLogStatus status,
    @Size(max = 2000) String note) {}
