package com.algorycode.rent.domain.user;

import com.algorycode.rent.domain.AbstractAuditableUuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "panel_users")
public class PanelUser extends AbstractAuditableUuidEntity {

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(nullable = false)
  private String email;

  @Column(name = "is_deleted", nullable = false)
  private boolean deleted;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private PanelUserRole role;

  @Column(name = "last_active_at", nullable = false)
  private Instant lastActiveAt;

  @Column(nullable = false)
  private boolean active = true;
}
