package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.CreateVehicleRequest;
import com.algorycode.rent.api.dto.UpdateVehicleRequest;
import com.algorycode.rent.api.dto.VehicleDto;
import com.algorycode.rent.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

  private final VehicleService vehicleService;

  public VehicleController(VehicleService vehicleService) {
    this.vehicleService = vehicleService;
  }

  @GetMapping
  public List<VehicleDto> list() {
    return vehicleService.listAll();
  }

  @GetMapping("/{id}")
  public VehicleDto get(@PathVariable UUID id) {
    return vehicleService.getById(id);
  }

  @PostMapping
  public VehicleDto create(@Valid @RequestBody CreateVehicleRequest body) {
    return vehicleService.create(body);
  }

  @PatchMapping("/{id}")
  public VehicleDto update(@PathVariable UUID id, @Valid @RequestBody UpdateVehicleRequest body) {
    return vehicleService.update(id, body);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    vehicleService.delete(id);
  }
}
