package com.algorycode.rent.domain.catalog;

import com.algorycode.rent.domain.AbstractAuditableLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "reservation_extra_option_templates")
public class ReservationExtraOptionTemplate extends AbstractAuditableLongEntity {

  @Column(nullable = false, length = 64)
  private String code;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal price = BigDecimal.ZERO;

  @Column(length = 512)
  private String icon;

  @Column(name = "line_order", nullable = false)
  private int lineOrder;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "requires_co_driver_details", nullable = false)
  private boolean requiresCoDriverDetails;
}
