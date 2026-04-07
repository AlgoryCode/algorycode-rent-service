package com.algorycode.rent.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Ortak UUID PK ve zaman damgası — tekrarlayan alanları DRY tutar.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractAuditableUuidEntity {

  @Id
  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(nullable = false, updatable = false, length = 36, columnDefinition = "CHAR(36)")
  private UUID id;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    var now = Instant.now();
    if (id == null) {
      id = UUID.randomUUID();
    }
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }
}
