package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.CreateVehicleRequest;
import com.algorycode.rent.api.dto.SimpleMessageResponse;
import com.algorycode.rent.api.dto.UpdateVehicleImageRequest;
import com.algorycode.rent.api.dto.UpdateVehicleRequest;
import com.algorycode.rent.api.dto.VehicleCalendarOccupancyDto;
import com.algorycode.rent.api.dto.VehicleDto;
import com.algorycode.rent.api.dto.VehicleFormCatalogDto;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.domain.vehicle.VehicleImageSlot;
import com.algorycode.rent.service.VehicleFormCatalogService;
import com.algorycode.rent.service.VehicleOccupancyService;
import com.algorycode.rent.service.VehicleService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

  private final VehicleService vehicleService;
  private final VehicleOccupancyService vehicleOccupancyService;
  private final VehicleFormCatalogService vehicleFormCatalogService;
  private final MessageSource messageSource;

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
  public ResponseEntity<SimpleMessageResponse> create(@Valid @RequestBody CreateVehicleRequest body) {
    Long id = vehicleService.create(body);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    String message =
        messageSource.getMessage("vehicle.created", null, LocaleContextHolder.getLocale());
    return ResponseEntity.status(HttpStatus.CREATED).location(location).body(new SimpleMessageResponse(message));
  }

  @PatchMapping("/{id:\\d+}")
  public VehicleDto update(@PathVariable Long id, @Valid @RequestBody UpdateVehicleRequest body) {
    return vehicleService.update(id, body);
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
