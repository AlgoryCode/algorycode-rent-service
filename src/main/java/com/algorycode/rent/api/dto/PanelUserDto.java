package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.user.PanelUserRole;

import java.time.Instant;
import java.util.UUID;

public record PanelUserDto(
    UUID id,
    String fullName,
    String email,
    PanelUserRole role,
    Instant lastActiveAt,
    boolean active) {}
