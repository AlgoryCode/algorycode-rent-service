package com.algorycode.rent.controller;

import com.algorycode.rent.dto.VehicleCatalogEntryDto;
import com.algorycode.rent.dto.VehicleLookupCreateRequest;
import com.algorycode.rent.dto.VehicleLookupUpdateRequest;
import com.algorycode.rent.service.VehicleFuelTypeService;
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
@RequestMapping("/vehicle-fuel-types")
@RequiredArgsConstructor
public class VehicleFuelTypeController {

  private final VehicleFuelTypeService vehicleFuelTypeService;

  @GetMapping
  public List<VehicleCatalogEntryDto> list() {
    return vehicleFuelTypeService.listAll();
  }

  @GetMapping("/{code}")
  public VehicleCatalogEntryDto get(@PathVariable String code) {
    return vehicleFuelTypeService.getByCode(code);
  }

  @PostMapping
  public VehicleCatalogEntryDto create(@Valid @RequestBody VehicleLookupCreateRequest body) {
    return vehicleFuelTypeService.create(body);
  }

  @PutMapping("/{code}")
  public VehicleCatalogEntryDto update(
      @PathVariable String code, @Valid @RequestBody VehicleLookupUpdateRequest body) {
    return vehicleFuelTypeService.update(code, body);
  }

  @DeleteMapping("/by-id/{id}")
  public void delete(@PathVariable long id) {
    vehicleFuelTypeService.delete(id);
  }
}
