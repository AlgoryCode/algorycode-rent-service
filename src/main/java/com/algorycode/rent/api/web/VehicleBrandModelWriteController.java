package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.CreateVehicleBrandRequest;
import com.algorycode.rent.api.dto.CreateVehicleModelRequest;
import com.algorycode.rent.api.dto.VehicleBrandCatalogDto;
import com.algorycode.rent.api.dto.VehicleModelCatalogDto;
import com.algorycode.rent.service.VehicleBrandModelWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rent/vehicle-brands")
@RequiredArgsConstructor
public class VehicleBrandModelWriteController {

  private final VehicleBrandModelWriteService vehicleBrandModelWriteService;

  @PostMapping
  public VehicleBrandCatalogDto createBrand(@Valid @RequestBody CreateVehicleBrandRequest body) {
    return vehicleBrandModelWriteService.createBrand(body.name(), body.sortOrder());
  }

  @PostMapping("/{brandId}/models")
  public VehicleModelCatalogDto createModel(
      @PathVariable long brandId, @Valid @RequestBody CreateVehicleModelRequest body) {
    return vehicleBrandModelWriteService.createModel(brandId, body.name(), body.sortOrder());
  }
}
