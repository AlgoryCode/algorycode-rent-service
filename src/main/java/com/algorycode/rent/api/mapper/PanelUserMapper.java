package com.algorycode.rent.api.mapper;

import com.algorycode.rent.api.dto.PanelUserDto;
import com.algorycode.rent.domain.user.PanelUser;

public final class PanelUserMapper {

  private PanelUserMapper() {}

  public static PanelUserDto toDto(PanelUser u) {
    return new PanelUserDto(
        u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.getLastActiveAt(), u.isActive());
  }
}
