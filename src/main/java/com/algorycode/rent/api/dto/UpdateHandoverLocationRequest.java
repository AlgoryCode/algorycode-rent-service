package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.location.HandoverLocationKind;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateHandoverLocationRequest(
    HandoverLocationKind kind,
    @Size(max = 255) String name,
    @Size(max = 4000) String description,
    @Size(max = 500) String addressLine,
    UUID cityId,
    /** {@code true} ise {@code cityId} yok sayılır ve şehir bağlantısı kaldırılır. */
    Boolean clearCity,
    Boolean active,
    Integer lineOrder) {}
