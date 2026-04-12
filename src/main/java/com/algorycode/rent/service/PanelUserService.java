package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.PanelUserDto;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.api.mapper.PanelUserMapper;
import com.algorycode.rent.repository.PanelUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PanelUserService {

  private final PanelUserRepository panelUserRepository;

  public PanelUserService(PanelUserRepository panelUserRepository) {
    this.panelUserRepository = panelUserRepository;
  }

  @Transactional(readOnly = true)
  public List<PanelUserDto> listAll() {
    return panelUserRepository.findAll().stream().map(PanelUserMapper::toDto).toList();
  }

  @Transactional
  public void deleteById(UUID id) {
    if (!panelUserRepository.existsById(id)) {
      throw new ResourceNotFoundException("Panel kullanıcısı bulunamadı: " + id);
    }
    panelUserRepository.deleteById(id);
  }
}
