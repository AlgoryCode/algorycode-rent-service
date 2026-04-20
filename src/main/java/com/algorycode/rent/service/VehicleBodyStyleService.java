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
    String code =
        VehicleCatalogSupport.resolveNewCatalogCode(
            req.code(),
            req.labelTr().trim(),
            true,
            c -> vehicleBodyStyleRepository.findByCodeIgnoreCase(c).isPresent());
    VehicleBodyStyle e = new VehicleBodyStyle();
    e.setCode(code);
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
        throw new BadRequestException("Özellik adı boş olamaz.");
      }
      e.setLabelTr(req.labelTr().trim());
    }
    if (req.sortOrder() != null) {
      e.setSortOrder(req.sortOrder());
    }
    return toDto(vehicleBodyStyleRepository.save(e));
  }

  @Transactional
  public void delete(long id) {
    VehicleBodyStyle e =
        vehicleBodyStyleRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Araç türü bulunamadı."));
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
    return new VehicleCatalogEntryDto(e.getId(), e.getCode(), e.getLabelTr(), e.getSortOrder());
  }
}
