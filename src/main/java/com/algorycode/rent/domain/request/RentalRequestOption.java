package com.algorycode.rent.domain.request;

import com.algorycode.rent.domain.AbstractAuditableLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rental_request_options")
public class RentalRequestOption extends AbstractAuditableLongEntity {

  @Column(name = "rental_request_id", nullable = false)
  private Long rentalRequestId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rental_request_id", nullable = false, insertable = false, updatable = false)
  private RentalRequest rentalRequest;

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

  @PrePersist
  @PreUpdate
  void syncRentalRequestOptionFk() {
    if (rentalRequest != null && rentalRequest.getId() != null) {
      rentalRequestId = rentalRequest.getId();
    }
  }
}
