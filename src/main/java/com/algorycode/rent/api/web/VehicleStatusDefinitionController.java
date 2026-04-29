package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.VehicleCatalogEntryDto;
import com.algorycode.rent.api.dto.VehicleLookupCreateRequest;
import com.algorycode.rent.api.dto.VehicleLookupUpdateRequest;
import com.algorycode.rent.service.VehicleStatusDefinitionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vehicle-statuses")
public class VehicleStatusDefinitionController {

  private final VehicleStatusDefinitionService vehicleStatusDefinitionService;

  public VehicleStatusDefinitionController(
      VehicleStatusDefinitionService vehicleStatusDefinitionService) {
    this.vehicleStatusDefinitionService = vehicleStatusDefinitionService;
  }

  @GetMapping
  public List<VehicleCatalogEntryDto> list() {
    return vehicleStatusDefinitionService.listAll();
  }

  @GetMapping("/{code}")
  public VehicleCatalogEntryDto get(@PathVariable String code) {
    return vehicleStatusDefinitionService.getByCode(code);
  }

  @PostMapping
  public VehicleCatalogEntryDto create(@Valid @RequestBody VehicleLookupCreateRequest body) {
    return vehicleStatusDefinitionService.create(body);
  }

  @PutMapping("/{code}")
  public VehicleCatalogEntryDto update(
      @PathVariable String code, @Valid @RequestBody VehicleLookupUpdateRequest body) {
    return vehicleStatusDefinitionService.update(code, body);
  }

  @DeleteMapping("/by-id/{id}")
  public void delete(@PathVariable long id) {
    vehicleStatusDefinitionService.delete(id);
  }
}
