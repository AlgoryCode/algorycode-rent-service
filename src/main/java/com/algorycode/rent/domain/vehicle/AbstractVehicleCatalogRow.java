package com.algorycode.rent.domain.vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Araç katalog satırları (gövde / yakıt / vites) için ortak alanlar — tekrarlayan entity tanımı ve servis
 * kalıplarını tek tip altında toplar.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractVehicleCatalogRow {

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
