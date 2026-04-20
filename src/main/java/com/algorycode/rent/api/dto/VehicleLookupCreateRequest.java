package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * @param code İsteğe bağlı; boş veya null ise sunucu {@code labelTr} üzerinden benzersiz kod üretir.
 */
public record VehicleLookupCreateRequest(
    @Size(max = 32)
        @Pattern(
            regexp = "^$|^[A-Za-z0-9_-]{1,32}$",
            message = "Kod en fazla 32 karakter; yalnız harf, rakam, tire ve alt çizgi veya boş bırakın.")
        String code,
    @NotBlank @Size(max = 128) String labelTr,
    @NotNull @Min(0) @Max(9999) Integer sortOrder) {}
