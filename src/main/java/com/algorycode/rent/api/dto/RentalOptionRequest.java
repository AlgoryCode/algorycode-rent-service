package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Kiralama veya talep üzerinde satır satır tutulan ek seçenek (ör. bebek koltuğu, ek sigorta).
 *
 * <p>Öncelik: {@code vehicleOptionDefinitionId} → araç tanımı; {@code reservationExtraTemplateId} →
 * rezervasyon ek şablonu; aksi halde {@code title}/{@code price} ile serbest satır.
 */
public record RentalOptionRequest(
    Long vehicleOptionDefinitionId,
    Long reservationExtraTemplateId,
    @Size(max = 255) String title,
    @Size(max = 4000) String description,
    @DecimalMin(value = "0", inclusive = true) BigDecimal price,
    /** İkon URL’si veya anahtar; şimdilik null olabilir. */
    @Size(max = 512) String icon) {}
