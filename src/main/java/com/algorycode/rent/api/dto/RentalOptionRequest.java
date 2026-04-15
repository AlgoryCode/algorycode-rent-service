package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Kiralama veya talep üzerinde satır satır tutulan ek seçenek (ör. bebek koltuğu, ek sigorta).
 *
 * <p>{@code vehicleOptionDefinitionId} doluysa başlık/fiyat sunucuda araç tanımından alınır; aksi halde
 * gövdedeki alanlar kullanılır.
 */
public record RentalOptionRequest(
    UUID vehicleOptionDefinitionId,
    @Size(max = 255) String title,
    @Size(max = 4000) String description,
    @DecimalMin(value = "0", inclusive = true) BigDecimal price,
    /** İkon URL’si veya anahtar; şimdilik null olabilir. */
    @Size(max = 512) String icon) {}
