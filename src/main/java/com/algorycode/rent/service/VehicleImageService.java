package com.algorycode.rent.service;

import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleImage;
import com.algorycode.rent.domain.vehicle.VehicleImageSlot;
import com.algorycode.rent.repository.VehicleRepository;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Araç görsel slotları: yükleme, doğrulama, toplu uygulama. */
@Service
public class VehicleImageService {

  private static final EnumSet<VehicleImageSlot> REQUIRED_IMAGE_SLOTS =
      EnumSet.of(
          VehicleImageSlot.front,
          VehicleImageSlot.rear,
          VehicleImageSlot.left,
          VehicleImageSlot.right,
          VehicleImageSlot.interiorDash,
          VehicleImageSlot.interiorRear);

  private final VehicleRepository vehicleRepository;
  private final ObjectStorageService objectStorageService;

  public VehicleImageService(VehicleRepository vehicleRepository, ObjectStorageService objectStorageService) {
    this.vehicleRepository = vehicleRepository;
    this.objectStorageService = objectStorageService;
  }

  public void validateRequiredVehicleImages(Map<String, String> images) {
    if (images == null) {
      throw new BadRequestException("Araç görselleri zorunludur (ön, arka, sol, sağ, kokpit, arka koltuk).");
    }
    for (VehicleImageSlot requiredSlot : REQUIRED_IMAGE_SLOTS) {
      String value = images.get(requiredSlot.name());
      if (value == null || value.isBlank()) {
        throw new BadRequestException("Araç görsellerinde " + requiredSlot.name() + " zorunludur.");
      }
    }
  }

  public void applyVehicleImages(Vehicle vehicle, Map<String, String> images) {
    removeStoredVehicleImages(vehicle);
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
              "vehicles/" + vehicle.getId() + "/" + slot.name(),
              slot.name(),
              imageValue.trim());
      VehicleImage img = new VehicleImage();
      img.setVehicle(vehicle);
      img.setSlot(slot);
      img.setImageUrl(storedKey);
      vehicle.getImages().add(img);
    }
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
}
