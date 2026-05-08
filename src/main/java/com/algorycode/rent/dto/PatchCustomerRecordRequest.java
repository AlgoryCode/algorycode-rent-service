package com.algorycode.rent.dto;

import jakarta.validation.constraints.NotNull;

public record PatchCustomerRecordRequest(@NotNull Boolean active) {}
