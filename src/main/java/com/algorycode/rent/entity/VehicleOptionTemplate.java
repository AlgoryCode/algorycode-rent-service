package com.algorycode.rent.entity;

import com.algorycode.rent.entity.AbstractAuditableLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vehicle_option_templates")
public class VehicleOptionTemplate extends AbstractAuditableLongEntity {

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
}
