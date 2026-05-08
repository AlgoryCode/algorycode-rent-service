package com.algorycode.rent.service;

import com.algorycode.rent.dto.VehicleCatalogEntryDto;
import com.algorycode.rent.dto.VehicleLookupCreateRequest;
import com.algorycode.rent.dto.VehicleLookupUpdateRequest;
import com.algorycode.rent.exception.BadRequestException;
import com.algorycode.rent.exception.ConflictException;
import com.algorycode.rent.exception.ResourceNotFoundException;
import com.algorycode.rent.entity.VehicleBodyStyle;
import com.algorycode.rent.repository.VehicleBodyStyleRepository;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.service.support.Text;
import com.algorycode.rent.service.support.VehicleCatalogSupport;
import com.algorycode.rent.service.vehiclecatalog.VehicleCatalogCrudPort;
import com.algorycode.rent.service.vehiclecatalog.VehicleCatalogEntityFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleBodyStyleService implements VehicleCatalogCrudPort {

  private final VehicleBodyStyleRepository vehicleBodyStyleRepository;
  private final VehicleRepository vehicleRepository;

  @Transactional(readOnly = true)
  public List<VehicleCatalogEntryDto> listAll() {
    return vehicleBodyStyleRepository.findAllByOrderBySortOrderAsc().stream()
        .map(VehicleBodyStyleService::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public VehicleCatalogEntryDto getByCode(String code) {
    return toDto(requireEntity(code));
  }

  @Transactional
  public VehicleCatalogEntryDto create(VehicleLookupCreateRequest req) {
    String code =
        VehicleCatalogSupport.resolveNewCatalogCode(
            req.code(),
            req.labelTr().trim(),
            true,
            c -> vehicleBodyStyleRepository.findByCodeIgnoreCase(c).isPresent());
    return toDto(
        vehicleBodyStyleRepository.save(
            VehicleCatalogEntityFactory.newBodyStyle(code, req.labelTr().trim(), req.sortOrder())));
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
    long used = vehicleRepository.countByBodyStyleIdAndDeletedFalse(e.getId());
    if (used > 0) {
      throw new ConflictException("Bu araç türü " + used + " araçta kullanılıyor; silinemez.");
    }
    vehicleBodyStyleRepository.delete(e);
  }

  private VehicleBodyStyle requireEntity(String rawCode) {
    String key =
        Optional.ofNullable(Text.trimOrNull(rawCode))
            .orElseThrow(() -> new BadRequestException("Kod gerekli."));
    return vehicleBodyStyleRepository
        .findByCodeIgnoreCase(key)
        .orElseThrow(() -> new ResourceNotFoundException("Araç türü bulunamadı: " + key));
  }

  private static VehicleCatalogEntryDto toDto(VehicleBodyStyle e) {
    return new VehicleCatalogEntryDto(e.getId(), e.getCode(), e.getLabelTr(), e.getSortOrder());
  }
}
