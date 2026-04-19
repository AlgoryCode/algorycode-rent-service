package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateVehicleRequest(
    @NotBlank @Size(max = 32) String plate,
    @NotBlank @Size(max = 255) String brand,
    @NotBlank @Size(max = 255) String model,
    @NotNull @Min(1950) @Max(2100) Integer year,
    boolean maintenance,
    /** Araç başka firmadan geldiyse işaretleyin. */
    boolean external,
    @Size(max = 255) String externalCompany,
    @NotNull @DecimalMin(value = "0.01", inclusive = true)
    BigDecimal rentalDailyPrice,
    @DecimalMin(value = "0.0", inclusive = true)
    BigDecimal commissionRatePercent,
    @Size(max = 32) String commissionBrokerPhone,
    /** ISO 3166-1 alpha-2 (örn. TR); zorunlu. Şehir yoksa araç yalnızca ülkeye bağlanır. */
    @NotBlank @Size(min = 2, max = 2) String countryCode,
    /** Opsiyonel; doluysa {@code countryCode} ile aynı ülkeye ait olmalıdır. */
    UUID cityId,
    /** Kiralama başlangıcında kullanılacak varsayılan alış noktası (PICKUP türü). */
    @NotNull UUID defaultPickupHandoverLocationId,
    /** Geriye uyumluluk: tek teslim noktası; {@code returnHandoverLocationIds} doluysa yok sayılır. */
    UUID defaultReturnHandoverLocationId,
    /**
     * Bu araca izin verilen teslim (RETURN) noktaları; sıra korunur. Boş veya null: araç bazlı teslim kısıtı yok
     * (yalnızca {@code defaultReturnHandoverLocationId} doluysa tek elemanlı liste gibi davranır).
     */
    @Size(max = 50) List<UUID> returnHandoverLocationIds,
    /** Şablondan kopyalanacak opsiyonlar (sıra korunur); ardından {@code optionDefinitions} eklenir. */
    @Size(max = 100) List<UUID> optionTemplateIds,
    @Size(max = 100) List<@Valid VehicleOptionDefinitionRequest> optionDefinitions,
    Map<String, String> images,
    @Size(max = 255) String engine,
    @Size(max = 64) String fuelType,
    @Size(max = 64) String bodyColor,
    @Min(1) @Max(20) Integer seats,
    @Min(0) Integer luggage,
    @Size(max = 32) String transmissionType,
    @Size(max = 32) String bodyStyleCode,
    /** Acente tarafından opsiyonel; liste sırası gösterim sırasıdır. */
    @Size(max = 30) List<@NotBlank @Size(max = 500) String> highlights) {}
