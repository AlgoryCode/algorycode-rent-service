package com.algorycode.rent.domain.vehicle;

import com.algorycode.rent.domain.AbstractAuditableUuidEntity;
import com.algorycode.rent.domain.location.City;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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

  /** Araç başka bir firmadan geldiyse işaretlenir. */
  @Column(name = "external_vehicle", nullable = false)
  private boolean external = false;

  @Column(name = "external_company", length = 255)
  private String externalCompany;

  /** Günlük kiralama fiyatı. */
  @Column(name = "rental_daily_price", precision = 12, scale = 2)
  private BigDecimal rentalDailyPrice;

  /** Araç için komisyon var/yok. */
  @Column(name = "commission_enabled", nullable = false)
  private boolean commissionEnabled = false;

  /** Komisyon oranı (yüzde). */
  @Column(name = "commission_rate_percent", precision = 5, scale = 2)
  private BigDecimal commissionRatePercent;

  /** Komisyoncu adı soyadı. */
  @Column(name = "commission_broker_full_name", length = 255)
  private String commissionBrokerFullName;

  /** Komisyoncu telefonu (opsiyonel). */
  @Column(name = "commission_broker_phone", length = 32)
  private String commissionBrokerPhone;

  /** Geçici geriye uyumluluk alanı (yeni modelde country city üzerinden okunur). */
  @Column(name = "country_code", length = 2)
  private String countryCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "city_id")
  private City city;

  @Column(name = "engine", length = 255)
  private String engine;

  @Column(name = "seats")
  private Integer seats;

  @Column(name = "luggage")
  private Integer luggage;

  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<VehicleImage> images = new ArrayList<>();
}
