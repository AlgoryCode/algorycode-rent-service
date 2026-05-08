package com.algorycode.rent.service;

import com.algorycode.rent.dto.CreateReservationExtraOptionTemplateRequest;
import com.algorycode.rent.dto.ReservationExtraOptionTemplateDto;
import com.algorycode.rent.dto.UpdateReservationExtraOptionTemplateRequest;
import com.algorycode.rent.exception.BadRequestException;
import com.algorycode.rent.exception.ResourceNotFoundException;
import com.algorycode.rent.entity.ReservationExtraOptionTemplate;
import com.algorycode.rent.repository.ReservationExtraOptionTemplateRepository;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationExtraOptionTemplateService {

  private final ReservationExtraOptionTemplateRepository repository;

  @Transactional(readOnly = true)
  public List<ReservationExtraOptionTemplateDto> list(boolean includeInactive) {
    if (includeInactive) {
      return repository.findAllByOrderByLineOrderAscTitleAsc().stream()
          .map(ReservationExtraOptionTemplateService::toDto)
          .toList();
    }
    return repository.findByActiveTrueOrderByLineOrderAscTitleAsc().stream()
        .map(ReservationExtraOptionTemplateService::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ReservationExtraOptionTemplateDto> listActive() {
    return list(false);
  }

  @Transactional(readOnly = true)
  public ReservationExtraOptionTemplate requireActive(Long id) {
    return repository
        .findByIdAndActiveTrue(id)
        .orElseThrow(() -> new ResourceNotFoundException("Rezervasyon ek seçeneği bulunamadı."));
  }

  @Transactional
  public ReservationExtraOptionTemplateDto create(CreateReservationExtraOptionTemplateRequest req) {
    String code = req.code().trim().toUpperCase();
    if (repository.existsByCodeIgnoreCase(code)) {
      throw new BadRequestException("Bu kod zaten kullanılıyor: " + code);
    }
    ReservationExtraOptionTemplate e = new ReservationExtraOptionTemplate();
    e.setCode(code);
    e.setTitle(req.title().trim());
    e.setDescription(
        req.description() != null && !req.description().isBlank()
            ? req.description().trim()
            : null);
    e.setPrice(req.price().setScale(2, RoundingMode.HALF_UP));
    e.setIcon(req.icon() != null && !req.icon().isBlank() ? req.icon().trim() : null);
    e.setLineOrder(req.lineOrder());
    e.setActive(req.active() == null || Boolean.TRUE.equals(req.active()));
    e.setRequiresCoDriverDetails(Boolean.TRUE.equals(req.requiresCoDriverDetails()));
    return toDto(repository.save(e));
  }

  @Transactional
  public ReservationExtraOptionTemplateDto update(
      Long id, UpdateReservationExtraOptionTemplateRequest req) {
    ReservationExtraOptionTemplate e =
        repository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Rezervasyon ek seçeneği bulunamadı."));
    if (req.code() != null) {
      String code = req.code().trim().toUpperCase();
      if (code.isBlank()) {
        throw new BadRequestException("Kod boş olamaz.");
      }
      if (!code.equalsIgnoreCase(e.getCode())
          && repository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
        throw new BadRequestException("Bu kod zaten kullanılıyor: " + code);
      }
      e.setCode(code);
    }
    if (req.title() != null) {
      e.setTitle(req.title().trim());
    }
    if (req.description() != null) {
      e.setDescription(req.description().isBlank() ? null : req.description().trim());
    }
    if (req.price() != null) {
      e.setPrice(req.price().setScale(2, RoundingMode.HALF_UP));
    }
    if (req.icon() != null) {
      e.setIcon(req.icon().isBlank() ? null : req.icon().trim());
    }
    if (req.lineOrder() != null) {
      e.setLineOrder(req.lineOrder());
    }
    if (req.active() != null) {
      e.setActive(req.active());
    }
    if (req.requiresCoDriverDetails() != null) {
      e.setRequiresCoDriverDetails(req.requiresCoDriverDetails());
    }
    return toDto(repository.save(e));
  }

  @Transactional
  public void deactivate(Long id) {
    ReservationExtraOptionTemplate e =
        repository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Rezervasyon ek seçeneği bulunamadı."));
    e.setActive(false);
    repository.save(e);
  }

  public static ReservationExtraOptionTemplateDto toDto(ReservationExtraOptionTemplate e) {
    return new ReservationExtraOptionTemplateDto(
        e.getId(),
        e.getCode(),
        e.getTitle(),
        e.getDescription(),
        e.getPrice(),
        e.getIcon(),
        e.getLineOrder(),
        e.isActive(),
        e.isRequiresCoDriverDetails());
  }
}
