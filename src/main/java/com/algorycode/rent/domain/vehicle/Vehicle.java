package com.algorycode.rent.domain.vehicle;

import com.algorycode.rent.domain.AbstractAuditableLongEntity;
import com.algorycode.rent.domain.location.City;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "vehicles")
public class Vehicle extends AbstractAuditableLongEntity {

  /** Silinmemiş kayıtlar arasında plaka benzersizliği serviste doğrulanır (yumuşak silinen plaka yeniden kullanılabilir). */
  @Column(nullable = false, length = 32)
  private String plate;

  @Column(name = "is_deleted", nullable = false)
  private boolean deleted;

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
  @Column(name = "country_code", length = 64)
  private String countryCode;

  @Column(name = "city_id")
  private Long cityId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "city_id", insertable = false, updatable = false)
  private City city;

  /** Varsayılan alış noktası; kiralama tamamlanınca son teslim noktasına güncellenebilir. */
  @Column(name = "default_pickup_handover_location_id")
  private Long defaultPickupHandoverLocationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "default_pickup_handover_location_id", insertable = false, updatable = false)
  private HandoverLocation defaultPickupHandoverLocation;

  /** Bu araç için müşterinin seçebileceği teslim (RETURN) noktaları; sıra {@code lineOrder}. */
  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("lineOrder ASC, id ASC")
  private List<VehicleAllowedReturnHandover> allowedReturnHandovers = new ArrayList<>();

  @Column(name = "engine", length = 255)
  private String engine;

  @Column(name = "fuel_type", length = 64)
  private String fuelType;

  @Column(name = "body_color", length = 64)
  private String bodyColor;

  @Column(name = "seats")
  private Integer seats;

  @Column(name = "luggage")
  private Integer luggage;

  /** Örn. {@code otomatik}, {@code manuel} (panel / arama ile uyumlu). */
  @Column(name = "transmission_type", length = 32)
  private String transmissionType;

  /** {@link VehicleBodyStyle#getCode()} — referans tablo. */
  @Column(name = "body_style_code", length = 32)
  private String bodyStyleCode;

  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<VehicleImage> images = new ArrayList<>();

  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("lineOrder ASC, title ASC")
  private List<VehicleOptionDefinition> optionDefinitions = new ArrayList<>();

  /** Öne çıkanlar (acente kaydında opsiyonel; sıra line_order). */
  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("lineOrder ASC")
  private List<VehicleHighlight> highlights = new ArrayList<>();

  /** user-fe vitrin paketi ({@code feFleetSnapshot}); {@link com.algorycode.rent.service.readmodel.FeFleetSnapshotBuilder}. */
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "fe_fleet_snapshot", columnDefinition = "json")
  private JsonNode feFleetSnapshot;

  @PrePersist
  @PreUpdate
  void syncLocationFks() {
    if (city != null && city.getId() != null) {
      cityId = city.getId();
    }
    if (defaultPickupHandoverLocation != null && defaultPickupHandoverLocation.getId() != null) {
      defaultPickupHandoverLocationId = defaultPickupHandoverLocation.getId();
    }
  }

  /** Kiralama / talep çözümlemesi için izin verilen RETURN handover kimlikleri (sıralı). */
  public List<Long> orderedReturnHandoverLocationIds() {
    if (allowedReturnHandovers == null || allowedReturnHandovers.isEmpty()) {
      return List.of();
    }
    return allowedReturnHandovers.stream()
        .sorted(
            Comparator.comparingInt(VehicleAllowedReturnHandover::getLineOrder)
                .thenComparing(VehicleAllowedReturnHandover::getId, Comparator.nullsLast(Comparator.naturalOrder())))
        .map(VehicleAllowedReturnHandover::getHandoverLocationId)
        .toList();
  }
}
