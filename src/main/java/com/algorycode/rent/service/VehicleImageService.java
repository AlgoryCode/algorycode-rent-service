package com.algorycode.rent.service;

import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleImage;
import com.algorycode.rent.domain.vehicle.VehicleImageSlot;
import com.algorycode.rent.repository.VehicleImageRepository;
import com.algorycode.rent.repository.VehicleRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleImageService {

  private final VehicleRepository vehicleRepository;
  private final VehicleImageRepository vehicleImageRepository;
  private final ObjectStorageService objectStorageService;
  private final TransactionTemplate transactionTemplate;

  public void applyVehicleImages(Vehicle vehicle, Map<String, String> images) {
    removeStoredVehicleImages(vehicle);
    if (images == null || images.isEmpty()) {
      return;
    }
    for (var e : images.entrySet()) {
      String imageValue = e.getValue();
      if (imageValue == null || imageValue.isBlank()) {
        continue;
      }
      final VehicleImageSlot slot;
      try {
        slot = VehicleImageSlot.valueOf(e.getKey());
      } catch (IllegalArgumentException ex) {
        throw new BadRequestException("Geçersiz görsel slotu: " + e.getKey());
      }
      String storedKey =
          objectStorageService.uploadDataUrl(
              "vehicles/" + vehicle.getId() + "/" + slot.name(), slot.name(), imageValue.trim());
      VehicleImage img = new VehicleImage();
      img.setVehicle(vehicle);
      img.setSlot(slot);
      img.setImageUrl(storedKey);
      vehicle.getImages().add(img);
    }
  }

  @Async("vehicleAsyncTaskExecutor")
  public void processVehicleImagesAsync(Long vehicleId, Map<String, String> images) {
    try {
      if (images == null || images.isEmpty()) {
        return;
      }
      List<ResolvedSlot> resolved = new ArrayList<>();
      for (var e : images.entrySet()) {
        if (e.getValue() == null || e.getValue().isBlank()) {
          continue;
        }
        try {
          resolved.add(new ResolvedSlot(VehicleImageSlot.valueOf(e.getKey()), e.getValue().trim()));
        } catch (IllegalArgumentException ex) {
          log.error(
              "Async vehicle images failed at stage=S3 vehicleId={} invalidSlot={}",
              vehicleId,
              e.getKey(),
              ex);
          return;
        }
      }
      if (resolved.isEmpty()) {
        return;
      }
      List<SlotKey> uploaded;
      try {
        List<CompletableFuture<SlotKey>> futures =
            resolved.stream()
                .map(
                    r ->
                        CompletableFuture.supplyAsync(
                            () -> uploadOne(vehicleId, r.slot(), r.imageValue())))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        uploaded = futures.stream().map(CompletableFuture::join).toList();
      } catch (Exception ex) {
        log.error(
            "Async vehicle images failed at stage=S3 vehicleId={} message={}",
            vehicleId,
            ex.getMessage(),
            ex);
        return;
      }
      try {
        transactionTemplate.executeWithoutResult(
            status -> persistVehicleImages(vehicleId, uploaded));
      } catch (Exception ex) {
        log.error(
            "Async vehicle images failed at stage=DB vehicleId={} message={}",
            vehicleId,
            ex.getMessage(),
            ex);
      }
    } catch (Exception ex) {
      log.error(
          "Async vehicle images failed vehicleId={} message={}", vehicleId, ex.getMessage(), ex);
    }
  }

  private SlotKey uploadOne(Long vehicleId, VehicleImageSlot slot, String imageValue) {
    String storedKey =
        objectStorageService.uploadDataUrl(
            "vehicles/" + vehicleId + "/" + slot.name(), slot.name(), imageValue);
    if (storedKey == null || storedKey.isBlank()) {
      throw new IllegalStateException("S3 upload returned empty key for slot " + slot.name());
    }
    return new SlotKey(slot, storedKey);
  }

  private void persistVehicleImages(Long vehicleId, List<SlotKey> uploaded) {
    Vehicle v =
        vehicleRepository
            .findByIdAndDeletedFalse(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));
    v.getImages().clear();
    List<VehicleImage> rows = new ArrayList<>();
    for (SlotKey sk : uploaded) {
      VehicleImage img = new VehicleImage();
      img.setVehicle(v);
      img.setSlot(sk.slot());
      img.setImageUrl(sk.storedKey());
      v.getImages().add(img);
      rows.add(img);
    }
    vehicleImageRepository.saveAll(rows);
    vehicleRepository.save(v);
  }

  @Transactional
  public Vehicle replaceImageSlot(Long vehicleId, VehicleImageSlot slot, String imageValue) {
    if (imageValue == null || imageValue.isBlank()) {
      throw new BadRequestException("Görsel verisi zorunludur.");
    }
    Vehicle v =
        vehicleRepository
            .findByIdAndDeletedFalse(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

    VehicleImage existing = null;
    for (VehicleImage img : v.getImages()) {
      if (img.getSlot() == slot) {
        existing = img;
        break;
      }
    }
    String newStored =
        objectStorageService.uploadDataUrl(
            "vehicles/" + v.getId() + "/" + slot.name(), slot.name(), imageValue.trim());
    if (newStored == null || newStored.isBlank()) {
      throw new BadRequestException("Görsel yüklenemedi.");
    }
    if (existing != null) {
      String oldRef = existing.getImageUrl();
      existing.setImageUrl(newStored);
      objectStorageService.deleteObjectIfStored(oldRef);
    } else {
      VehicleImage img = new VehicleImage();
      img.setVehicle(v);
      img.setSlot(slot);
      img.setImageUrl(newStored);
      v.getImages().add(img);
    }
    return vehicleRepository.save(v);
  }

  @Transactional
  public Vehicle deleteImageSlot(Long vehicleId, VehicleImageSlot slot) {
    Vehicle v =
        vehicleRepository
            .findByIdAndDeletedFalse(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

    VehicleImage existing = null;
    for (VehicleImage img : v.getImages()) {
      if (img.getSlot() == slot) {
        existing = img;
        break;
      }
    }
    if (existing == null) {
      throw new ResourceNotFoundException("Bu slotta görsel yok: " + slot.name());
    }
    objectStorageService.deleteObjectIfStored(existing.getImageUrl());
    v.getImages().remove(existing);
    existing.setVehicle(null);
    return vehicleRepository.save(v);
  }

  private void removeStoredVehicleImages(Vehicle vehicle) {
    List<VehicleImage> copy = new ArrayList<>(vehicle.getImages());
    for (VehicleImage img : copy) {
      objectStorageService.deleteObjectIfStored(img.getImageUrl());
    }
    vehicle.getImages().clear();
  }

  private record ResolvedSlot(VehicleImageSlot slot, String imageValue) {}

  private record SlotKey(VehicleImageSlot slot, String storedKey) {}
}
