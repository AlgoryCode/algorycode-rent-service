package com.algorycode.rent.dto;

import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * İstemci tarafından gönderilen fiyat kalemi özeti (şimdilik yalnızca denetim / log; asıl tutarlar
 * sunucuda yeniden hesaplanır).
 */
public record PricedLineRequest(
    @Size(max = 40) String lineType,
    Integer quantity,
    BigDecimal unitAmount,
    BigDecimal lineAmount,
    @Size(max = 3) String currency,
    @Size(max = 255) String title,
    @Size(max = 4000) String description,
    @Size(max = 8000) String metadata) {}
