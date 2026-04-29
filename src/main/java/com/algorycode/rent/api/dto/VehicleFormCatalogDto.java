package com.algorycode.rent.api.dto;

import java.util.List;

public record VehicleFormCatalogDto(
    List<VehicleBrandCatalogDto> brands,
    List<VehicleCatalogEntryDto> fuelTypes,
    List<VehicleCatalogEntryDto> transmissionTypes,
    List<VehicleCatalogEntryDto> bodyStyles,
    List<VehicleCatalogEntryDto> vehicleStatuses,
    List<CountryDto> countries,
    List<HandoverLocationDto> pickupHandoverLocations,
    List<HandoverLocationDto> returnHandoverLocations,
    List<VehicleOptionTemplateDto> optionTemplates) {}
