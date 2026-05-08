package com.algorycode.rent.controller;

import com.algorycode.rent.dto.VehicleCatalogEntryDto;
import com.algorycode.rent.dto.VehicleLookupCreateRequest;
import com.algorycode.rent.dto.VehicleLookupUpdateRequest;
import com.algorycode.rent.service.VehicleStatusCatalogService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rent/vehicle-statuses")
@RequiredArgsConstructor
public class VehicleStatusCatalogController {

  private final VehicleStatusCatalogService vehicleStatusCatalogService;

  @GetMapping
  public List<VehicleCatalogEntryDto> list() {
    return vehicleStatusCatalogService.listAll();
  }

  @GetMapping("/{code}")
  public VehicleCatalogEntryDto get(@PathVariable String code) {
    return vehicleStatusCatalogService.getByCode(code);
  }

  @PostMapping
  public VehicleCatalogEntryDto create(@Valid @RequestBody VehicleLookupCreateRequest body) {
    return vehicleStatusCatalogService.create(body);
  }

  @PutMapping("/{code}")
  public VehicleCatalogEntryDto update(
      @PathVariable String code, @Valid @RequestBody VehicleLookupUpdateRequest body) {
    return vehicleStatusCatalogService.update(code, body);
  }

  @DeleteMapping("/by-id/{id}")
  public void delete(@PathVariable long id) {
    vehicleStatusCatalogService.delete(id);
  }
}
