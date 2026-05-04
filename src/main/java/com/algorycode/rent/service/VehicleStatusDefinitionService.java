package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.VehicleCatalogEntryDto;
import com.algorycode.rent.api.dto.VehicleLookupCreateRequest;
import com.algorycode.rent.api.dto.VehicleLookupUpdateRequest;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.vehicle.VehicleStatusDefinition;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.repository.VehicleStatusDefinitionRepository;
import com.algorycode.rent.service.support.Text;
import com.algorycode.rent.service.support.VehicleCatalogSupport;
import com.algorycode.rent.service.vehiclecatalog.VehicleCatalogEntityFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleStatusDefinitionService {

  private final VehicleStatusDefinitionRepository vehicleStatusDefinitionRepository;
  private final VehicleRepository vehicleRepository;

  @Transactional(readOnly = true)
  public List<VehicleCatalogEntryDto> listAll() {
    return vehicleStatusDefinitionRepository
        .findAll(Sort.by(Sort.Direction.ASC, "sortOrder"))
        .stream()
        .map(VehicleStatusDefinitionService::toDto)
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
            false,
            c -> vehicleStatusDefinitionRepository.findByCodeIgnoreCase(c).isPresent());
    return toDto(
        vehicleStatusDefinitionRepository.save(
            VehicleCatalogEntityFactory.newVehicleStatusDefinition(
                code, req.labelTr().trim(), req.sortOrder())));
  }

  @Transactional
  public VehicleCatalogEntryDto update(String code, VehicleLookupUpdateRequest req) {
    VehicleCatalogSupport.requireUpdateHasSomething(req.labelTr(), req.sortOrder());
    VehicleStatusDefinition e = requireEntity(code);
    if (req.labelTr() != null) {
      if (req.labelTr().isBlank()) {
        throw new BadRequestException("Özellik adı boş olamaz.");
      }
      e.setLabelTr(req.labelTr().trim());
    }
    if (req.sortOrder() != null) {
      e.setSortOrder(req.sortOrder());
    }
    return toDto(vehicleStatusDefinitionRepository.save(e));
  }

  @Transactional
  public void delete(long id) {
    VehicleStatusDefinition e =
        vehicleStatusDefinitionRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Araç statüsü bulunamadı."));
    long used = vehicleRepository.countByStatusDefinition_IdAndDeletedFalse(e.getId());
    if (used > 0) {
      throw new ConflictException("Bu araç statüsü " + used + " araçta kullanılıyor; silinemez.");
    }
    vehicleStatusDefinitionRepository.delete(e);
  }

  private VehicleStatusDefinition requireEntity(String rawCode) {
    String key =
        Optional.ofNullable(Text.trimOrNull(rawCode))
            .orElseThrow(() -> new BadRequestException("Kod gerekli."));
    return vehicleStatusDefinitionRepository
        .findByCodeIgnoreCase(key)
        .orElseThrow(() -> new ResourceNotFoundException("Araç statüsü bulunamadı: " + key));
  }

  private static VehicleCatalogEntryDto toDto(VehicleStatusDefinition e) {
    return new VehicleCatalogEntryDto(e.getId(), e.getCode(), e.getLabelTr(), e.getSortOrder());
  }
}
