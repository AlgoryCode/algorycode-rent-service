package com.algorycode.rent.domain.location;

import com.algorycode.rent.domain.AbstractAuditableUuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "handover_locations")
public class HandoverLocation extends AbstractAuditableUuidEntity {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private HandoverLocationKind kind;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "address_line", length = 500)
  private String addressLine;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "city_id")
  private City city;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "line_order", nullable = false)
  private int lineOrder;
}
