package com.algorycode.rent.api.web;

import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.dto.CreateVehicleRequest;
import com.algorycode.rent.api.dto.UpdateVehicleImageRequest;
import com.algorycode.rent.api.dto.UpdateVehicleRequest;
import com.algorycode.rent.api.dto.VehicleCalendarOccupancyDto;
import com.algorycode.rent.api.dto.VehicleDto;
import com.algorycode.rent.domain.vehicle.VehicleImageSlot;
import com.algorycode.rent.service.VehicleOccupancyService;
import com.algorycode.rent.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

  private final VehicleService vehicleService;
  private final VehicleOccupancyService vehicleOccupancyService;

  public VehicleController(VehicleService vehicleService, VehicleOccupancyService vehicleOccupancyService) {
    this.vehicleService = vehicleService;
    this.vehicleOccupancyService = vehicleOccupancyService;
  }

  /**
   * Araç takvimi: iptal olmayan kiralamalar + reddedilmemiş talepler (pending/approved). Yalnızca
   * bu endpoint her iki kaynağı birleştirir; {@code GET /rentals} yalnızca kesin kira,
   * {@code GET /rental-requests} yalnızca talepleri döndürür. Her aralıkta {@code startDate} ve
   * {@code endDate} <strong>dahil</strong> (19–21 seçimi → üç gün de dolu sayılmalı).
   */
  @GetMapping("/{id}/calendar/occupancy")
  public VehicleCalendarOccupancyDto calendarOccupancy(
      @PathVariable UUID id, @RequestParam LocalDate from, @RequestParam LocalDate to) {
    return vehicleOccupancyService.occupancy(id, from, to);
  }

  /**
   * Tarih + alış/teslim noktası: {@code availableFrom}, {@code availableTo} (YYYY-MM-DD) birlikte
   * verilirse yalnız uygun araçlar döner. İptal olmayan kiralamalar ile pending/approved talepler aynı
   * çakışma + tampon kuralından geçer. Tampon: bitiş gününün ertesi günü başka kiralama
   * olmamalı. Alış: araç {@code defaultPickupHandoverLocation}. Teslim: izinli return listesi doluysa
   * bu noktada aranır; liste boşsa teslim kısıtı yok sayılır (tüm RETURN noktalarıyla uyumlu).
   * {@code includePartialAvailability}: true iken tam aralık dolu olsa bile, seçilen aralığın
   * başında alış günü + ertesi gün için müsait araçlar da listelenir (varsayılan false).
   */
  @GetMapping
  public List<VehicleDto> list(
      @RequestParam(required = false) LocalDate availableFrom,
      @RequestParam(required = false) LocalDate availableTo,
      @RequestParam(required = false) UUID pickupHandoverLocationId,
      @RequestParam(required = false) UUID returnHandoverLocationId,
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
