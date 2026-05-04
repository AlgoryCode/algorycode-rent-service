package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.VehicleCatalogEntryDto;
import com.algorycode.rent.api.dto.VehicleLookupCreateRequest;
import com.algorycode.rent.api.dto.VehicleLookupUpdateRequest;
import com.algorycode.rent.service.VehicleTransmissionTypeService;
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
@RequestMapping("/vehicle-transmission-types")
@RequiredArgsConstructor
public class VehicleTransmissionTypeController {

  private final VehicleTransmissionTypeService vehicleTransmissionTypeService;

  @GetMapping
  public List<VehicleCatalogEntryDto> list() {
    return vehicleTransmissionTypeService.listAll();
  }

  @GetMapping("/{code}")
  public VehicleCatalogEntryDto get(@PathVariable String code) {
    return vehicleTransmissionTypeService.getByCode(code);
  }

  @PostMapping
  public VehicleCatalogEntryDto create(@Valid @RequestBody VehicleLookupCreateRequest body) {
    return vehicleTransmissionTypeService.create(body);
  }

  @PutMapping("/{code}")
  public VehicleCatalogEntryDto update(
      @PathVariable String code, @Valid @RequestBody VehicleLookupUpdateRequest body) {
    return vehicleTransmissionTypeService.update(code, body);
  }

  @DeleteMapping("/by-id/{id}")
  public void delete(@PathVariable long id) {
    vehicleTransmissionTypeService.delete(id);
  }
}
