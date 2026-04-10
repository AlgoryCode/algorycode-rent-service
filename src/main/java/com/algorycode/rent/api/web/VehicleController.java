package com.algorycode.rent.api.web;

import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.dto.CreateVehicleRequest;
import com.algorycode.rent.api.dto.UpdateVehicleImageRequest;
import com.algorycode.rent.api.dto.UpdateVehicleRequest;
import com.algorycode.rent.api.dto.VehicleDto;
import com.algorycode.rent.domain.vehicle.VehicleImageSlot;
import com.algorycode.rent.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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

  /** Tek slot görseli: data URL veya mevcut object referansı ile günceller, eski nesneyi S3’ten siler. */
  @PutMapping("/{id}/images/{slot}")
  public VehicleDto replaceImage(
      @PathVariable UUID id,
      @PathVariable String slot,
      @Valid @RequestBody UpdateVehicleImageRequest body) {
    return vehicleService.replaceImageSlot(id, parseImageSlot(slot), body.image());
  }

  /** Tek slot görselini kaldırır (DB + object storage). */
  @DeleteMapping("/{id}/images/{slot}")
  public VehicleDto deleteImage(@PathVariable UUID id, @PathVariable String slot) {
    return vehicleService.deleteImageSlot(id, parseImageSlot(slot));
  }

  private static VehicleImageSlot parseImageSlot(String slot) {
    try {
      return VehicleImageSlot.valueOf(slot);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Geçersiz görsel slotu: " + slot);
    }
  }
}
