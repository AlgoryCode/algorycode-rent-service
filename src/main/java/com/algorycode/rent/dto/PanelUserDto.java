package com.algorycode.rent.dto;

import com.algorycode.rent.entity.PanelUserRole;
import java.time.Instant;

public record PanelUserDto(
    Long id,
    String fullName,
    String email,
    PanelUserRole role,
    Instant lastActiveAt,
    boolean active) {}
