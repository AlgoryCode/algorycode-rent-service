package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.VehicleCatalogEntryDto;
import com.algorycode.rent.api.dto.VehicleLookupCreateRequest;
import com.algorycode.rent.api.dto.VehicleLookupUpdateRequest;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.vehicle.VehicleBodyStyle;
import com.algorycode.rent.repository.VehicleBodyStyleRepository;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.service.support.Text;
import com.algorycode.rent.service.support.VehicleCatalogSupport;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleBodyStyleService {

  private final VehicleBodyStyleRepository vehicleBodyStyleRepository;
  private final VehicleRepository vehicleRepository;

  public VehicleBodyStyleService(
      VehicleBodyStyleRepository vehicleBodyStyleRepository, VehicleRepository vehicleRepository) {
    this.vehicleBodyStyleRepository = vehicleBodyStyleRepository;
    this.vehicleRepository = vehicleRepository;
  }

  @Transactional(readOnly = true)
  public List<VehicleCatalogEntryDto> listAll() {
    return vehicleBodyStyleRepository.findAllByOrderBySortOrderAsc().stream()
        .map(VehicleBodyStyleService::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public VehicleCatalogEntryDto getByCode(String code) {
    VehicleBodyStyle e = requireEntity(code);
    return toDto(e);
  }

  @Transactional
  public VehicleCatalogEntryDto create(VehicleLookupCreateRequest req) {
    String normalized = VehicleCatalogSupport.normalizeBodyStyleCode(req.code());
    if (vehicleBodyStyleRepository.findByCodeIgnoreCase(normalized).isPresent()) {
      throw new ConflictException("Bu araç türü kodu zaten kayıtlı: " + normalized);
    }
    VehicleBodyStyle e = new VehicleBodyStyle();
    e.setCode(normalized);
    e.setLabelTr(req.labelTr().trim());
    e.setSortOrder(req.sortOrder());
    return toDto(vehicleBodyStyleRepository.save(e));
  }

  @Transactional
  public VehicleCatalogEntryDto update(String code, VehicleLookupUpdateRequest req) {
    VehicleCatalogSupport.requireUpdateHasSomething(req.labelTr(), req.sortOrder());
    VehicleBodyStyle e = requireEntity(code);
    if (req.labelTr() != null) {
      if (req.labelTr().isBlank()) {
        throw new BadRequestException("labelTr boş olamaz.");
      }
      e.setLabelTr(req.labelTr().trim());
    }
    if (req.sortOrder() != null) {
      e.setSortOrder(req.sortOrder());
    }
    return toDto(vehicleBodyStyleRepository.save(e));
  }

  @Transactional
  public void delete(String code) {
    VehicleBodyStyle e = requireEntity(code);
    long used = vehicleRepository.countByBodyStyleCodeAndDeletedFalse(e.getCode());
    if (used > 0) {
      throw new ConflictException("Bu araç türü " + used + " araçta kullanılıyor; silinemez.");
    }
    vehicleBodyStyleRepository.delete(e);
  }

  private VehicleBodyStyle requireEntity(String rawCode) {
    String key = Text.trimOrNull(rawCode);
    if (key == null) {
      throw new BadRequestException("Kod gerekli.");
    }
    return vehicleBodyStyleRepository
        .findByCodeIgnoreCase(key)
        .orElseThrow(() -> new ResourceNotFoundException("Araç türü bulunamadı: " + rawCode));
  }

  private static VehicleCatalogEntryDto toDto(VehicleBodyStyle e) {
    return new VehicleCatalogEntryDto(e.getCode(), e.getLabelTr(), e.getSortOrder());
  }
}
