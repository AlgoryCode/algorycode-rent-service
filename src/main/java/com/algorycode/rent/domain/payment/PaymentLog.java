package com.algorycode.rent.domain.payment;

import com.algorycode.rent.domain.AbstractAuditableLongEntity;
import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.vehicle.Vehicle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "payment_logs")
public class PaymentLog extends AbstractAuditableLongEntity {

  @Column(name = "amount_try", nullable = false, precision = 14, scale = 2)
  private BigDecimal amountTry;

  @Enumerated(EnumType.STRING)
  @Column(name = "money_flow", nullable = false, length = 16)
  private PaymentMoneyFlow moneyFlow = PaymentMoneyFlow.inbound;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private PaymentLogStatus status;

  @Column(nullable = false, length = 128)
  private String method;

  @Column(nullable = false, length = 32)
  private String plate;

  @Column(name = "vehicle_id")
  private Long vehicleId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vehicle_id", insertable = false, updatable = false)
  private Vehicle vehicle;

  @Column(name = "rental_id")
  private Long rentalId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rental_id", insertable = false, updatable = false)
  private Rental rental;

  /** Kiralama anindaki toplam gelir (EUR): gunluk fiyat * gun + opsiyonlar. */
  @Column(name = "rental_revenue_eur", precision = 14, scale = 2)
  private BigDecimal rentalRevenueEur;

  @Column(name = "customer_name", nullable = false)
  private String customerName;

  @Column(nullable = false, unique = true, length = 64)
  private String reference;

  @Column(length = 2000)
  private String note;

  @PrePersist
  @PreUpdate
  void syncPaymentLogFks() {
    if (vehicle != null && vehicle.getId() != null) {
      vehicleId = vehicle.getId();
    }
    if (rental != null && rental.getId() != null) {
      rentalId = rental.getId();
    }
  }
}
