package com.algorycode.rent.domain.coupon;

import com.algorycode.rent.domain.AbstractAuditableLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "discount_coupons")
public class DiscountCoupon extends AbstractAuditableLongEntity {

  @Column(nullable = false, length = 64)
  private String code;

  @Column(name = "discount_type", nullable = false, length = 16)
  private String discountType;

  @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
  private BigDecimal discountValue;

  @Column(length = 255)
  private String description;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "usage_limit")
  private Integer usageLimit;

  @Column(name = "usage_count", nullable = false)
  private int usageCount = 0;

  @Column(name = "expires_at")
  private Instant expiresAt;
}
