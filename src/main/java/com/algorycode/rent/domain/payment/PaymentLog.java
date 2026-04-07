package com.algorycode.rent.domain.payment;

import com.algorycode.rent.domain.AbstractAuditableUuidEntity;
import com.algorycode.rent.domain.vehicle.Vehicle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "payment_logs")
public class PaymentLog extends AbstractAuditableUuidEntity {

  @Column(name = "amount_try", nullable = false, precision = 14, scale = 2)
  private BigDecimal amountTry;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private PaymentLogStatus status;

  @Column(nullable = false, length = 128)
  private String method;

  @Column(nullable = false, length = 32)
  private String plate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vehicle_id")
  private Vehicle vehicle;

  @Column(name = "customer_name", nullable = false)
  private String customerName;

  @Column(nullable = false, unique = true, length = 64)
  private String reference;

  @Column(length = 2000)
  private String note;
}
