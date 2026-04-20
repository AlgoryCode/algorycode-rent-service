package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.PanelUserDto;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.api.mapper.PanelUserMapper;
import com.algorycode.rent.repository.PanelUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PanelUserService {

  private final PanelUserRepository panelUserRepository;

  public PanelUserService(PanelUserRepository panelUserRepository) {
    this.panelUserRepository = panelUserRepository;
  }

  @Transactional(readOnly = true)
  public List<PanelUserDto> listAll() {
    return panelUserRepository.findAllByDeletedFalse().stream().map(PanelUserMapper::toDto).toList();
  }

  @Transactional
  public void deleteById(Long id) {
    var user =
        panelUserRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Panel kullanıcısı bulunamadı: " + id));
    user.setDeleted(true);
    panelUserRepository.save(user);
  }
}
