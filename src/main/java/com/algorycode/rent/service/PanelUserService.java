package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.PanelUserDto;
import com.algorycode.rent.api.mapper.PanelUserMapper;
import com.algorycode.rent.repository.PanelUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PanelUserService {

  private final PanelUserRepository panelUserRepository;

  public PanelUserService(PanelUserRepository panelUserRepository) {
    this.panelUserRepository = panelUserRepository;
  }

  public List<PanelUserDto> listAll() {
    return panelUserRepository.findAll().stream().map(PanelUserMapper::toDto).toList();
  }
}
