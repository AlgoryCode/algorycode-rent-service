package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.NotNull;

public record PatchCustomerRecordRequest(@NotNull Boolean active) {}
