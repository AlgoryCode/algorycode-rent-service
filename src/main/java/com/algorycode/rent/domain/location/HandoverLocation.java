package com.algorycode.rent.domain.location;

import com.algorycode.rent.domain.AbstractAuditableLongEntity;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Alış veya teslim noktası; şehir / ülke tablolarına FK yoktur. İsteğe bağlı {@code countryCode}
 * yalnızca ücretlendirme ve vitrin amaçlıdır.
 */
@Getter
@Setter
@Entity
@Table(name = "handover_locations")
public class HandoverLocation extends AbstractAuditableLongEntity {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private HandoverLocationKind kind;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "address_line", length = 500)
  private String addressLine;

  /** ISO ülke kodu vb.; şehir/ülke tablosu ile FK yok. */
  @Column(name = "country_code", length = 64)
  private String countryCode;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "line_order", nullable = false)
  private int lineOrder;

  /** Bu nokta seçildiğinde (rolü: alış veya iade) eklenecek sabit ek ücret (EUR). */
  @Column(name = "surcharge_eur", nullable = false, precision = 10, scale = 2)
  private BigDecimal surchargeEur = BigDecimal.ZERO;

  /** user-fe hero satırı (JSON). */
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "fe_handover_snapshot", columnDefinition = "jsonb")
  private JsonNode feHandoverSnapshot;
}
