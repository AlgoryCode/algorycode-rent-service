package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.HandoverLocationDto;
import com.algorycode.rent.api.dto.VehicleBrandCatalogDto;
import com.algorycode.rent.api.dto.VehicleCatalogEntryDto;
import com.algorycode.rent.api.dto.VehicleFormCatalogDto;
import com.algorycode.rent.api.dto.VehicleModelCatalogDto;
import com.algorycode.rent.domain.location.HandoverLocationKind;
import com.algorycode.rent.repository.VehicleBrandRepository;
import com.algorycode.rent.repository.VehicleStatusDefinitionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleFormCatalogServiceImpl implements VehicleFormCatalogService {

  private final VehicleBrandRepository vehicleBrandRepository;
  private final VehicleStatusDefinitionRepository vehicleStatusDefinitionRepository;
  private final CountryService countryService;
  private final HandoverLocationService handoverLocationService;
  private final VehicleOptionTemplateService vehicleOptionTemplateService;

  @Override
  @Transactional(readOnly = true)
  public VehicleFormCatalogDto load() {
    List<VehicleBrandCatalogDto> brands =
        vehicleBrandRepository.findAllWithModelsForCatalog().stream()
            .map(
                b ->
                    new VehicleBrandCatalogDto(
                        b.getId(),
                        b.getName(),
                        b.getSortOrder(),
                        b.getModels().stream()
                            .map(
                                m ->
                                    new VehicleModelCatalogDto(
                                        m.getId(), m.getName(), m.getSortOrder()))
                            .toList()))
            .toList();

    List<VehicleCatalogEntryDto> statuses =
        vehicleStatusDefinitionRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder")).stream()
            .map(
                e ->
                    new VehicleCatalogEntryDto(
                        e.getId(), e.getCode(), e.getLabelTr(), e.getSortOrder()))
            .toList();

    List<HandoverLocationDto> pickups =
        handoverLocationService.list(HandoverLocationKind.PICKUP, false);
    List<HandoverLocationDto> returns =
        handoverLocationService.list(HandoverLocationKind.RETURN, false);

    return new VehicleFormCatalogDto(
        brands,
        statuses,
        countryService.listAll(),
        pickups,
        returns,
        vehicleOptionTemplateService.list(false));
  }
}
