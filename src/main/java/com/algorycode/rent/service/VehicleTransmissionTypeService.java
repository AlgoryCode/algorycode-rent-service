package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.VehicleCatalogEntryDto;
import com.algorycode.rent.api.dto.VehicleLookupCreateRequest;
import com.algorycode.rent.api.dto.VehicleLookupUpdateRequest;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.vehicle.VehicleTransmissionType;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.repository.VehicleTransmissionTypeRepository;
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
public class VehicleTransmissionTypeService implements VehicleCatalogCrudPort {

  private final VehicleTransmissionTypeRepository vehicleTransmissionTypeRepository;
  private final VehicleRepository vehicleRepository;

  @Transactional(readOnly = true)
  public List<VehicleCatalogEntryDto> listAll() {
    return vehicleTransmissionTypeRepository.findAllByOrderBySortOrderAsc().stream()
        .map(VehicleTransmissionTypeService::toDto)
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
            c -> vehicleTransmissionTypeRepository.findByCodeIgnoreCase(c).isPresent());
    return toDto(
        vehicleTransmissionTypeRepository.save(
            VehicleCatalogEntityFactory.newTransmissionType(
                code, req.labelTr().trim(), req.sortOrder())));
  }

  @Transactional
  public VehicleCatalogEntryDto update(String code, VehicleLookupUpdateRequest req) {
    VehicleCatalogSupport.requireUpdateHasSomething(req.labelTr(), req.sortOrder());
    VehicleTransmissionType e = requireEntity(code);
    if (req.labelTr() != null) {
      if (req.labelTr().isBlank()) {
        throw new BadRequestException("Özellik adı boş olamaz.");
      }
      e.setLabelTr(req.labelTr().trim());
    }
    if (req.sortOrder() != null) {
      e.setSortOrder(req.sortOrder());
    }
    return toDto(vehicleTransmissionTypeRepository.save(e));
  }

  @Transactional
  public void delete(long id) {
    VehicleTransmissionType e =
        vehicleTransmissionTypeRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vites türü bulunamadı."));
    long used = vehicleRepository.countByTransmissionTypeRef_IdAndDeletedFalse(e.getId());
    if (used > 0) {
      throw new ConflictException("Bu vites türü " + used + " araçta kullanılıyor; silinemez.");
    }
    vehicleTransmissionTypeRepository.delete(e);
  }

  private VehicleTransmissionType requireEntity(String rawCode) {
    String key =
        Optional.ofNullable(Text.trimOrNull(rawCode))
            .orElseThrow(() -> new BadRequestException("Kod gerekli."));
    return vehicleTransmissionTypeRepository
        .findByCodeIgnoreCase(key)
        .orElseThrow(() -> new ResourceNotFoundException("Vites türü bulunamadı: " + key));
  }

  private static VehicleCatalogEntryDto toDto(VehicleTransmissionType e) {
    return new VehicleCatalogEntryDto(e.getId(), e.getCode(), e.getLabelTr(), e.getSortOrder());
  }
}
