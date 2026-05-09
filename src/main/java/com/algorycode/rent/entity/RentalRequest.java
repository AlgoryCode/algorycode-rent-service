package com.algorycode.rent.entity;

import com.algorycode.rent.entity.AbstractAuditableLongEntity;
import com.algorycode.rent.entity.HandoverLocation;
import com.algorycode.rent.entity.Vehicle;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rental_requests")
public class RentalRequest extends AbstractAuditableLongEntity {

  @Column(name = "reference_no", nullable = false, unique = true, length = 32)
  private String referenceNo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private RentalRequestStatus status = RentalRequestStatus.pending;

  @Column(name = "vehicle_id")
  private Long vehicleId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vehicle_id", insertable = false, updatable = false)
  private Vehicle vehicle;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  /** Talep anında hesaplanan kiralama gecesi (alış–teslim tarihleri ile uyumlu). */
  @Column(name = "rental_nights")
  private Integer rentalNights;

  /** {@code rental_request_priced_lines} satırlarının TRY toplamı (bilgilendirme / rapor). */
  @Column(name = "pricing_total_try", precision = 12, scale = 2)
  private BigDecimal pricingTotalTry;

  /** Kiralama baslangic saati (PDF Issue Date yanindaki Time). */
  @Column(name = "start_time")
  private LocalTime startTime;

  /** Iade saati (PDF Return Date yanindaki Time). */
  @Column(name = "return_time")
  private LocalTime returnTime;

  @Column(name = "pickup_handover_location_id")
  private Long pickupHandoverLocationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pickup_handover_location_id", insertable = false, updatable = false)
  private HandoverLocation pickupHandoverLocation;

  @Column(name = "return_handover_location_id")
  private Long returnHandoverLocationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "return_handover_location_id", insertable = false, updatable = false)
  private HandoverLocation returnHandoverLocation;

  @Column(name = "outside_country_travel", nullable = false)
  private boolean outsideCountryTravel;

  @Column(name = "green_insurance_fee", nullable = false, precision = 12, scale = 2)
  private BigDecimal greenInsuranceFee = BigDecimal.ZERO;

  @Column(name = "status_message", length = 500)
  private String statusMessage;

  @Column(name = "note", length = 1000)
  private String note;

  @Column(name = "contract_pdf_path", length = 512)
  private String contractPdfPath;

  @Column(name = "whatsapp_contract_sent_at")
  private Instant whatsappContractSentAt;

  @Column(name = "whatsapp_contract_error", length = 500)
  private String whatsappContractError;

  @Embedded private RentalRequestCustomerSnapshot customer;

  @Column(name = "user_id")
  private UUID userId;

  @OneToMany(mappedBy = "rentalRequest", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RentalRequestAdditionalDriver> additionalDrivers = new ArrayList<>();

  @OneToMany(mappedBy = "rentalRequest", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("lineOrder ASC")
  private List<RentalRequestOption> options = new ArrayList<>();

  @OneToMany(mappedBy = "rentalRequest", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("lineOrder ASC")
  private List<RentalRequestPricedLine> pricedLines = new ArrayList<>();

  /** Talep oluşturulurken hesaplanan alış noktası ek ücreti (EUR). */
  @Column(name = "handover_pickup_leg_eur", nullable = false, precision = 10, scale = 2)
  private BigDecimal handoverPickupLegEur = BigDecimal.ZERO;

  @Column(name = "handover_return_leg_eur", nullable = false, precision = 10, scale = 2)
  private BigDecimal handoverReturnLegEur = BigDecimal.ZERO;

  @Column(name = "handover_route_eur", nullable = false, precision = 10, scale = 2)
  private BigDecimal handoverRouteEur = BigDecimal.ZERO;

  @Column(name = "handover_total_eur", nullable = false, precision = 10, scale = 2)
  private BigDecimal handoverTotalEur = BigDecimal.ZERO;

  @PrePersist
  @PreUpdate
  void syncRequestFks() {
    if (vehicle != null) {
      vehicleId = vehicle.getId();
    }
    if (pickupHandoverLocation != null) {
      pickupHandoverLocationId = pickupHandoverLocation.getId();
    }
    if (returnHandoverLocation != null) {
      returnHandoverLocationId = returnHandoverLocation.getId();
    }
  }
}
