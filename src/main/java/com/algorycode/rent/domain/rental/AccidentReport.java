package com.algorycode.rent.domain.rental;

import com.algorycode.rent.domain.AbstractAuditableLongEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "accident_reports")
public class AccidentReport extends AbstractAuditableLongEntity {

  @Column(name = "rental_id", nullable = false)
  private Long rentalId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rental_id", nullable = false, insertable = false, updatable = false)
  private Rental rental;

  @Column(nullable = false)
  private Instant at;

  @Column(nullable = false, length = 4000)
  private String description;

  @OneToMany(mappedBy = "accidentReport", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AccidentPhoto> photos = new ArrayList<>();

  @PrePersist
  @PreUpdate
  void syncAccidentReportFk() {
    if (rental != null && rental.getId() != null) {
      rentalId = rental.getId();
    }
  }
}
