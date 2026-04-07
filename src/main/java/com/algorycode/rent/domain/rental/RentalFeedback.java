package com.algorycode.rent.domain.rental;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/** Kiralama başına en fazla bir yorum (FE ile uyumlu). */
@Getter
@Setter
@Entity
@Table(name = "rental_feedback")
public class RentalFeedback {

  @Id
  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(length = 36, columnDefinition = "CHAR(36)")
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId
  @JoinColumn(name = "rental_id")
  private Rental rental;

  @Column(nullable = false)
  private Instant at;

  @Column(name = "feedback_text", nullable = false, length = 4000)
  private String text;
}
