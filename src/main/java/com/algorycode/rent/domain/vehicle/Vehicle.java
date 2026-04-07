package com.algorycode.rent.domain.vehicle;

import com.algorycode.rent.domain.AbstractAuditableUuidEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "vehicles")
public class Vehicle extends AbstractAuditableUuidEntity {

  @Column(nullable = false, unique = true, length = 32)
  private String plate;

  @Column(nullable = false, length = 255)
  private String brand;

  @Column(nullable = false, length = 255)
  private String model;

  @Column(nullable = false)
  private Integer year;

  @Column(nullable = false)
  private boolean maintenance = false;

  /** ISO 3166-1 alpha-2; ülke listesi / satır rengi için. */
  @Column(name = "country_code", length = 2)
  private String countryCode;

  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<VehicleImage> images = new ArrayList<>();
}
