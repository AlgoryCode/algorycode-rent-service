package com.algorycode.rent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "vehicle_brands")
public class VehicleBrand {

  @EqualsAndHashCode.Include
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
  @OrderBy("sortOrder ASC, name ASC")
  private List<VehicleModel> models = new ArrayList<>();
}
