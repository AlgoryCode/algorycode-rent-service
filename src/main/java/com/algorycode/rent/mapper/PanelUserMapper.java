package com.algorycode.rent.mapper;

import com.algorycode.rent.dto.PanelUserDto;
import com.algorycode.rent.entity.PanelUser;

public final class PanelUserMapper {

  private PanelUserMapper() {}

  public static PanelUserDto toDto(PanelUser u) {
    return new PanelUserDto(
        u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.getLastActiveAt(), u.isActive());
  }
}
