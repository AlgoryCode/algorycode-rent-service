package com.algorycode.rent.domain.request;

import com.algorycode.rent.domain.AbstractAuditableUuidEntity;
import com.algorycode.rent.domain.vehicle.Vehicle;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "rental_requests")
public class RentalRequest extends AbstractAuditableUuidEntity {

  @Column(name = "reference_no", nullable = false, unique = true, length = 32)
  private String referenceNo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private RentalRequestStatus status = RentalRequestStatus.pending;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vehicle_id")
  private Vehicle vehicle;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

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

  @Embedded
  private RentalRequestCustomerSnapshot customer;

  @Column(name = "user_id")
  private UUID userId;

  @OneToMany(mappedBy = "rentalRequest", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RentalRequestAdditionalDriver> additionalDrivers = new ArrayList<>();
}
