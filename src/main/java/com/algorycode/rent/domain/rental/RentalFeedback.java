package com.algorycode.rent.domain.rental;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Kiralama başına en fazla bir yorum (FE ile uyumlu). PK: otomatik artan {@code id}; {@code rental_id} benzersiz FK. */
@Getter
@Setter
@Entity
@Table(name = "rental_feedback")
public class RentalFeedback {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "rental_id", nullable = false, unique = true)
  private Long rentalId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rental_id", nullable = false, insertable = false, updatable = false)
  private Rental rental;

  @Column(nullable = false)
  private Instant at;

  @Column(name = "feedback_text", nullable = false, length = 4000)
  private String text;

  @PrePersist
  @PreUpdate
  void syncRentalFk() {
    if (rental != null && rental.getId() != null) {
      rentalId = rental.getId();
    }
  }
}
