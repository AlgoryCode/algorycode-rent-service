package com.algorycode.rent.dto;

import java.util.List;

public record VehicleBrandCatalogDto(
    long id, String name, int sortOrder, List<VehicleModelCatalogDto> models) {}
