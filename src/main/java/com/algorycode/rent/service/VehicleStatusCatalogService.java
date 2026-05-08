package com.algorycode.rent.service;

import com.algorycode.rent.dto.VehicleCatalogEntryDto;
import com.algorycode.rent.dto.VehicleLookupCreateRequest;
import com.algorycode.rent.dto.VehicleLookupUpdateRequest;
import com.algorycode.rent.exception.BadRequestException;
import com.algorycode.rent.exception.ConflictException;
import com.algorycode.rent.exception.ResourceNotFoundException;
import com.algorycode.rent.entity.VehicleStatusCatalog;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.repository.VehicleStatusCatalogRepository;
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
public class VehicleStatusCatalogService {

  private final VehicleStatusCatalogRepository vehicleStatusCatalogRepository;
  private final VehicleRepository vehicleRepository;

  @Transactional(readOnly = true)
  public List<VehicleCatalogEntryDto> listAll() {
    return vehicleStatusCatalogRepository
        .findAll(Sort.by(Sort.Direction.ASC, "sortOrder"))
        .stream()
        .map(VehicleStatusCatalogService::toDto)
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
            c -> vehicleStatusCatalogRepository.findByCodeIgnoreCase(c).isPresent());
    return toDto(
        vehicleStatusCatalogRepository.save(
            VehicleCatalogEntityFactory.newVehicleStatusCatalog(
                code, req.labelTr().trim(), req.sortOrder())));
  }

  @Transactional
  public VehicleCatalogEntryDto update(String code, VehicleLookupUpdateRequest req) {
    VehicleCatalogSupport.requireUpdateHasSomething(req.labelTr(), req.sortOrder());
    VehicleStatusCatalog e = requireEntity(code);
    if (req.labelTr() != null) {
      if (req.labelTr().isBlank()) {
        throw new BadRequestException("Özellik adı boş olamaz.");
      }
      e.setLabelTr(req.labelTr().trim());
    }
    if (req.sortOrder() != null) {
      e.setSortOrder(req.sortOrder());
    }
    return toDto(vehicleStatusCatalogRepository.save(e));
  }

  @Transactional
  public void delete(long id) {
    VehicleStatusCatalog e =
        vehicleStatusCatalogRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Araç statüsü bulunamadı."));
    long used = vehicleRepository.countByVehicleStatusIdAndDeletedFalse(e.getId());
    if (used > 0) {
      throw new ConflictException("Bu araç statüsü " + used + " araçta kullanılıyor; silinemez.");
    }
    vehicleStatusCatalogRepository.delete(e);
  }

  private VehicleStatusCatalog requireEntity(String rawCode) {
    String key =
        Optional.ofNullable(Text.trimOrNull(rawCode))
            .orElseThrow(() -> new BadRequestException("Kod gerekli."));
    return vehicleStatusCatalogRepository
        .findByCodeIgnoreCase(key)
        .orElseThrow(() -> new ResourceNotFoundException("Araç statüsü bulunamadı: " + key));
  }

  private static VehicleCatalogEntryDto toDto(VehicleStatusCatalog e) {
    return new VehicleCatalogEntryDto(e.getId(), e.getCode(), e.getLabelTr(), e.getSortOrder());
  }
}
