package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.VehicleCatalogEntryDto;
import com.algorycode.rent.api.dto.VehicleLookupCreateRequest;
import com.algorycode.rent.api.dto.VehicleLookupUpdateRequest;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.vehicle.VehicleFuelType;
import com.algorycode.rent.repository.VehicleFuelTypeRepository;
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
public class VehicleFuelTypeService implements VehicleCatalogCrudPort {

  private final VehicleFuelTypeRepository vehicleFuelTypeRepository;
  private final VehicleRepository vehicleRepository;

  @Transactional(readOnly = true)
  public List<VehicleCatalogEntryDto> listAll() {
    return vehicleFuelTypeRepository.findAllByOrderBySortOrderAsc().stream()
        .map(VehicleFuelTypeService::toDto)
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
            c -> vehicleFuelTypeRepository.findByCodeIgnoreCase(c).isPresent());
    return toDto(
        vehicleFuelTypeRepository.save(
            VehicleCatalogEntityFactory.newFuelType(code, req.labelTr().trim(), req.sortOrder())));
  }

  @Transactional
  public VehicleCatalogEntryDto update(String code, VehicleLookupUpdateRequest req) {
    VehicleCatalogSupport.requireUpdateHasSomething(req.labelTr(), req.sortOrder());
    VehicleFuelType e = requireEntity(code);
    if (req.labelTr() != null) {
      if (req.labelTr().isBlank()) {
        throw new BadRequestException("Özellik adı boş olamaz.");
      }
      e.setLabelTr(req.labelTr().trim());
    }
    if (req.sortOrder() != null) {
      e.setSortOrder(req.sortOrder());
    }
    return toDto(vehicleFuelTypeRepository.save(e));
  }

  @Transactional
  public void delete(long id) {
    VehicleFuelType e =
        vehicleFuelTypeRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Yakıt türü bulunamadı."));
    long used = vehicleRepository.countByFuelTypeAndDeletedFalse(e.getCode());
    if (used > 0) {
      throw new ConflictException("Bu yakıt türü " + used + " araçta kullanılıyor; silinemez.");
    }
    vehicleFuelTypeRepository.delete(e);
  }

  private VehicleFuelType requireEntity(String rawCode) {
    String key =
        Optional.ofNullable(Text.trimOrNull(rawCode))
            .orElseThrow(() -> new BadRequestException("Kod gerekli."));
    return vehicleFuelTypeRepository
        .findByCodeIgnoreCase(key)
        .orElseThrow(() -> new ResourceNotFoundException("Yakıt türü bulunamadı: " + key));
  }

  private static VehicleCatalogEntryDto toDto(VehicleFuelType e) {
    return new VehicleCatalogEntryDto(e.getId(), e.getCode(), e.getLabelTr(), e.getSortOrder());
  }
}
