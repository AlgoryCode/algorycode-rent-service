package com.algorycode.rent.dto;

import java.util.List;

public record VehicleFormCatalogDto(
    List<VehicleBrandCatalogDto> brands,
    List<VehicleCatalogEntryDto> vehicleStatuses,
    List<CountryDto> countries,
    List<HandoverLocationDto> pickupHandoverLocations,
    List<HandoverLocationDto> returnHandoverLocations,
    List<VehicleOptionTemplateDto> optionTemplates) {}
