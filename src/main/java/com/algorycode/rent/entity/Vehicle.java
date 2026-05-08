package com.algorycode.rent.entity;

import com.algorycode.rent.entity.AbstractAuditableLongEntity;
import com.algorycode.rent.entity.HandoverLocation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Getter
@Setter
@Builder
@Entity
@Table(name = "vehicles")
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle extends AbstractAuditableLongEntity {

  /**
   * Silinmemiş kayıtlar arasında plaka benzersizliği serviste doğrulanır (yumuşak silinen plaka
   * yeniden kullanılabilir).
   */
  @Column(length = 32, nullable = false)
  private String plate;

  @Column(name = "is_deleted", nullable = false)
  private boolean deleted;

  private Integer year;

  @Column(name = "vehicle_status_id", nullable = false)
  private Long vehicleStatusId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vehicle_status_id", nullable = false, insertable = false, updatable = false)
  private VehicleStatusCatalog vehicleStatus;

  @Column(name = "vehicle_model_id")
  private Long vehicleModelId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vehicle_model_id", insertable = false, updatable = false)
  private VehicleModel vehicleModel;

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

  /** Araç kayıtlı ülke kodu (şehir FK’si yok). */
  @Column(name = "country_code", length = 64)
  private String countryCode;

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

  @Column(name = "fuel_type_id")
  private Long fuelTypeId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fuel_type_id", insertable = false, updatable = false)
  private VehicleFuelType fuelTypeRef;

  @Column(name = "body_color", length = 64)
  private String bodyColor;

  @Column(name = "seats")
  private Integer seats;

  @Column(name = "luggage")
  private Integer luggage;

  @Column(name = "transmission_type_id")
  private Long transmissionTypeId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transmission_type_id", insertable = false, updatable = false)
  private VehicleTransmissionType transmissionTypeRef;

  @Column(name = "body_style_id")
  private Long bodyStyleId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "body_style_id", insertable = false, updatable = false)
  private VehicleBodyStyle bodyStyleRef;

  @Fetch(FetchMode.SUBSELECT)
  @BatchSize(size = 32)
  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<VehicleImage> images = new ArrayList<>();

  @Fetch(FetchMode.SUBSELECT)
  @BatchSize(size = 32)
  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("lineOrder ASC, title ASC")
  private List<VehicleOptionDefinition> optionDefinitions = new ArrayList<>();

  @Fetch(FetchMode.SUBSELECT)
  @BatchSize(size = 32)
  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("lineOrder ASC")
  private List<VehicleHighlight> highlights = new ArrayList<>();

  public VehicleStatus getStatus() {
    if (vehicleStatus == null) {
      return VehicleStatus.active;
    }
    return VehicleStatus.fromDbCode(vehicleStatus.getCode());
  }

  public String getTransmissionTypeCode() {
    return transmissionTypeRef == null ? null : transmissionTypeRef.getCode();
  }

  public String getBodyStyleCode() {
    return bodyStyleRef == null ? null : bodyStyleRef.getCode();
  }

  public String getFuelType() {
    return fuelTypeRef == null ? null : fuelTypeRef.getCode();
  }

  public String getBrand() {
    if (vehicleModel == null || vehicleModel.getBrand() == null) {
      return "";
    }
    return Optional.ofNullable(vehicleModel.getBrand().getName()).orElse("");
  }

  public String getModel() {
    if (vehicleModel == null) {
      return "";
    }
    return Optional.ofNullable(vehicleModel.getName()).orElse("");
  }

  public List<Long> orderedReturnHandoverLocationIds() {
    if (allowedReturnHandovers.isEmpty()) {
      return List.of();
    }
    return allowedReturnHandovers.stream()
        .sorted(
            Comparator.comparingInt(VehicleAllowedReturnHandover::getLineOrder)
                .thenComparing(
                    VehicleAllowedReturnHandover::getId,
                    Comparator.nullsLast(Comparator.naturalOrder())))
        .map(VehicleAllowedReturnHandover::getHandoverLocationId)
        .toList();
  }
}
