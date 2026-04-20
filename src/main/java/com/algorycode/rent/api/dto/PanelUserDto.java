package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.user.PanelUserRole;

import java.time.Instant;

public record PanelUserDto(
    Long id,
    String fullName,
    String email,
    PanelUserRole role,
    Instant lastActiveAt,
    boolean active) {}
