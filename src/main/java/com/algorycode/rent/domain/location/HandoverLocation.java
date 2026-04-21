package com.algorycode.rent.domain.location;

import com.algorycode.rent.domain.AbstractAuditableLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

  @Column(name = "city_id")
  private Long cityId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "city_id", insertable = false, updatable = false)
  private City city;

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

  @PrePersist
  @PreUpdate
  void syncCityFk() {
    if (city != null && city.getId() != null) {
      cityId = city.getId();
    }
  }
}
