package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CreateVehicleOptionTemplateRequest;
import com.algorycode.rent.api.dto.UpdateVehicleOptionTemplateRequest;
import com.algorycode.rent.api.dto.VehicleOptionTemplateDto;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.vehicle.VehicleOptionTemplate;
import com.algorycode.rent.repository.VehicleOptionTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;

@Service
public class VehicleOptionTemplateService {

  private final VehicleOptionTemplateRepository vehicleOptionTemplateRepository;

  public VehicleOptionTemplateService(VehicleOptionTemplateRepository vehicleOptionTemplateRepository) {
    this.vehicleOptionTemplateRepository = vehicleOptionTemplateRepository;
  }

  @Transactional(readOnly = true)
  public List<VehicleOptionTemplateDto> list(boolean includeInactive) {
    if (includeInactive) {
      return vehicleOptionTemplateRepository.findAllByOrderByLineOrderAscTitleAsc().stream()
          .map(VehicleOptionTemplateService::toDto)
          .toList();
    }
    return vehicleOptionTemplateRepository.findByActiveTrueOrderByLineOrderAscTitleAsc().stream()
        .map(VehicleOptionTemplateService::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public VehicleOptionTemplate requireActive(Long id) {
    return vehicleOptionTemplateRepository
        .findByIdAndActiveTrue(id)
        .orElseThrow(() -> new BadRequestException("Opsiyon şablonu bulunamadı veya pasif: " + id));
  }

  @Transactional
  public VehicleOptionTemplateDto create(CreateVehicleOptionTemplateRequest req) {
    VehicleOptionTemplate e = new VehicleOptionTemplate();
    e.setTitle(req.title().trim());
    e.setDescription(
        req.description() != null && !req.description().isBlank() ? req.description().trim() : null);
    e.setPrice(req.price().setScale(2, RoundingMode.HALF_UP));
    e.setIcon(req.icon() != null && !req.icon().isBlank() ? req.icon().trim() : null);
    e.setLineOrder(req.lineOrder());
    e.setActive(req.active() == null || Boolean.TRUE.equals(req.active()));
    return toDto(vehicleOptionTemplateRepository.save(e));
  }

  @Transactional
  public VehicleOptionTemplateDto update(Long id, UpdateVehicleOptionTemplateRequest req) {
    VehicleOptionTemplate e =
        vehicleOptionTemplateRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Opsiyon şablonu bulunamadı: " + id));
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
    return toDto(vehicleOptionTemplateRepository.save(e));
  }

  @Transactional
  public void deactivate(Long id) {
    VehicleOptionTemplate e =
        vehicleOptionTemplateRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Opsiyon şablonu bulunamadı: " + id));
    e.setActive(false);
    vehicleOptionTemplateRepository.save(e);
  }

  private static VehicleOptionTemplateDto toDto(VehicleOptionTemplate e) {
    return new VehicleOptionTemplateDto(
        e.getId(),
        e.getTitle(),
        e.getDescription(),
        e.getPrice(),
        e.getIcon(),
        e.getLineOrder(),
        e.isActive());
  }
}
