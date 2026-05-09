package com.algorycode.rent.controller;

import com.algorycode.rent.dto.CreateVehicleRequest;
import com.algorycode.rent.dto.UpdateVehicleImageRequest;
import com.algorycode.rent.dto.UpdateVehicleRequest;
import com.algorycode.rent.dto.VehicleCalendarOccupancyDto;
import com.algorycode.rent.dto.VehicleDto;
import com.algorycode.rent.dto.VehicleFormCatalogDto;
import com.algorycode.rent.exception.BadRequestException;
import com.algorycode.rent.entity.VehicleImageSlot;
import com.algorycode.rent.entity.VehicleStatus;
import com.algorycode.rent.service.VehicleFormCatalogService;
import com.algorycode.rent.service.VehicleOccupancyService;
import com.algorycode.rent.service.VehicleService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

  private final VehicleService vehicleService;
  private final VehicleOccupancyService vehicleOccupancyService;
  private final VehicleFormCatalogService vehicleFormCatalogService;

  @GetMapping("/{id:\\d+}/calendar/occupancy")
  public VehicleCalendarOccupancyDto calendarOccupancy(
      @PathVariable Long id, @RequestParam LocalDate from, @RequestParam LocalDate to) {
    return vehicleOccupancyService.occupancy(id, from, to);
  }

  @GetMapping("/form-catalog")
  public VehicleFormCatalogDto formCatalog() {
    return vehicleFormCatalogService.load();
  }

  @GetMapping
  public List<VehicleDto> list(
      @RequestParam(required = false) LocalDate availableFrom,
      @RequestParam(required = false) LocalDate availableTo,
      @RequestParam(required = false) Long pickupHandoverLocationId,
      @RequestParam(required = false) Long returnHandoverLocationId,
      @RequestParam(required = false) Boolean includePartialAvailability) {
    if (availableFrom == null
        && availableTo == null
        && pickupHandoverLocationId == null
        && returnHandoverLocationId == null) {
      return vehicleService.listAll();
    }
    if ((pickupHandoverLocationId != null || returnHandoverLocationId != null)
        && (availableFrom == null || availableTo == null)) {
      throw new BadRequestException(
          "pickupHandoverLocationId veya returnHandoverLocationId için availableFrom ve availableTo zorunludur.");
    }
    if ((availableFrom == null) != (availableTo == null)) {
      throw new BadRequestException("availableFrom ve availableTo birlikte verilmelidir.");
    }
    return vehicleService.listWithAvailabilityFilter(
        availableFrom,
        availableTo,
        pickupHandoverLocationId,
        returnHandoverLocationId,
        Boolean.TRUE.equals(includePartialAvailability));
  }

  @GetMapping("/{id:\\d+}")
  public VehicleDto get(@PathVariable Long id) {
    return vehicleService.getById(id);
  }

  @PostMapping
  public ResponseEntity<String> create(
      @Valid @RequestBody CreateVehicleRequest body) {
    vehicleService.create(body);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body("Vehicle created successfully");
  }

  @PatchMapping("/{id:\\d+}")
  public ResponseEntity<String> update(
      @PathVariable Long id, @Valid @RequestBody UpdateVehicleRequest body) {
    vehicleService.update(id, body);
    return ResponseEntity.ok("Updated Successfully");
  }

  @PatchMapping("/{id:\\d+}/status")
  public VehicleDto updateVehicleStatus(
      @PathVariable Long id, @RequestParam VehicleStatus status) {
    return vehicleService.updateVehicleStatus(id, status);
  }

  @DeleteMapping("/{id:\\d+}")
  public void delete(@PathVariable Long id) {
    vehicleService.delete(id);
  }

  @PutMapping("/{id:\\d+}/images/{slot}")
  public VehicleDto replaceImage(
      @PathVariable Long id,
      @PathVariable String slot,
      @Valid @RequestBody UpdateVehicleImageRequest body) {
    return vehicleService.replaceImageSlot(id, parseImageSlot(slot), body.image());
  }

  @DeleteMapping("/{id:\\d+}/images/{slot}")
  public VehicleDto deleteImage(@PathVariable Long id, @PathVariable String slot) {
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
