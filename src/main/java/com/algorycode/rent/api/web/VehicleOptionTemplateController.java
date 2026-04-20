package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.CreateVehicleOptionTemplateRequest;
import com.algorycode.rent.api.dto.UpdateVehicleOptionTemplateRequest;
import com.algorycode.rent.api.dto.VehicleOptionTemplateDto;
import com.algorycode.rent.service.VehicleOptionTemplateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vehicle-option-templates")
public class VehicleOptionTemplateController {

  private final VehicleOptionTemplateService vehicleOptionTemplateService;

  public VehicleOptionTemplateController(VehicleOptionTemplateService vehicleOptionTemplateService) {
    this.vehicleOptionTemplateService = vehicleOptionTemplateService;
  }

  @GetMapping
  public List<VehicleOptionTemplateDto> list(
      @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
    return vehicleOptionTemplateService.list(includeInactive);
  }

  @PostMapping
  public VehicleOptionTemplateDto create(@Valid @RequestBody CreateVehicleOptionTemplateRequest body) {
    return vehicleOptionTemplateService.create(body);
  }

  @PatchMapping("/{id}")
  public VehicleOptionTemplateDto update(
      @PathVariable Long id, @Valid @RequestBody UpdateVehicleOptionTemplateRequest body) {
    return vehicleOptionTemplateService.update(id, body);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deactivate(@PathVariable Long id) {
    vehicleOptionTemplateService.deactivate(id);
  }
}
