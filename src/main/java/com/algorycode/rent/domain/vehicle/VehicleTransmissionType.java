package com.algorycode.rent.domain.vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vehicle_transmission_types")
public class VehicleTransmissionType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "code", nullable = false, unique = true, length = 32)
  private String code;

  @Column(name = "label_tr", nullable = false, length = 128)
  private String labelTr;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;
}
