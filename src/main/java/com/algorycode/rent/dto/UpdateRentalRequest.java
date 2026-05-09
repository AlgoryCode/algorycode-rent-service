package com.algorycode.rent.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UpdateRentalRequest(
    LocalDate startDate,
    LocalDate endDate,
    Long pickupHandoverLocationId,
    Long returnHandoverLocationId,
    @DecimalMin(value = "0", inclusive = true) BigDecimal discountAmount,
    @Size(max = 16) String discountType,
    @Size(max = 64) String status,
    CustomerRequest customer,
    @Size(max = 100) List<@NotNull Long> vehicleOptionDefinitionIds,
    @Size(max = 100) List<@NotNull Long> reservationExtraTemplateIds) {}
