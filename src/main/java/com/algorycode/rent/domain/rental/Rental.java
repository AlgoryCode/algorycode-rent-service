package com.algorycode.rent.domain.rental;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "rentals")
public class Rental extends AbstractAuditableUuidEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vehicle_id", nullable = false)
  private Vehicle vehicle;

  @Column(nullable = false)
  private LocalDate startDate;

  @Column(nullable = false)
  private LocalDate endDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private RentalStatus status = RentalStatus.active;

  @Embedded
  private CustomerSnapshot customer;

  @OneToOne(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private RentalFeedback feedback;

  @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RentalPhoto> photos = new ArrayList<>();

  @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AccidentReport> accidentReports = new ArrayList<>();
}
