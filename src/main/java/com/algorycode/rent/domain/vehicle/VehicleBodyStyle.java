package com.algorycode.rent.domain.vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vehicle_body_styles")
public class VehicleBodyStyle {

  @Id
  @Column(length = 32)
  private String code;

  @Column(name = "label_tr", nullable = false, length = 128)
  private String labelTr;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;
}
