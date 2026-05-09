package com.algorycode.rent.service;

import com.algorycode.rent.exception.ResourceNotFoundException;
import com.algorycode.rent.entity.RentalStatus;
import com.algorycode.rent.entity.Vehicle;
import com.algorycode.rent.entity.VehicleStatus;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.repository.VehicleStatusCatalogRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleCatalogStatusService {

  private final VehicleRepository vehicleRepository;
  private final VehicleStatusCatalogRepository vehicleStatusCatalogRepository;

  @Transactional
  public void updateVehicleFromRentalStatus(Long vehicleId, RentalStatus rentalStatus) {
    updateVehicleStatus(vehicleId, vehicleStatusForRental(rentalStatus));
  }

  @Transactional
  public void updateVehicleStatus(Long vehicleId, VehicleStatus status) {
    Vehicle vehicle =
        vehicleRepository
            .findByIdAndDeletedFalse(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));
    if (Objects.equals(vehicle.getStatus(), status)) {
      return;
    }
    vehicle.setVehicleStatus(status);
    vehicleRepository.save(vehicle);
  }

  public VehicleStatus vehicleStatusForRental(RentalStatus rentalStatus) {
    return switch (rentalStatus) {
      case ACTIVE, PENDING -> VehicleStatus.RENTED;
      case COMPLETED, CANCELLED -> VehicleStatus.ACTIVE;
    };
  }

  @Transactional
  public void updateVehicleCatalogStatus(Long vehicleId, String catalogCode) {
    Vehicle vehicle =
        vehicleRepository
            .findByIdAndDeletedFalse(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));
    applyCatalogCodeToVehicle(vehicle, catalogCode);
    vehicleRepository.save(vehicle);
  }

  public boolean applyCatalogCodeToVehicle(Vehicle vehicle, String catalogCode) {
    vehicleStatusCatalogRepository
        .findByCodeIgnoreCase(catalogCode)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Vehicle status catalog row missing for code: " + catalogCode));
    VehicleStatus target = VehicleStatus.fromCatalogCode(catalogCode.trim());
    if (Objects.equals(vehicle.getStatus(), target)) {
      return false;
    }
    vehicle.setVehicleStatus(target);
    return true;
  }

  public String catalogCodeForRentalStatus(RentalStatus rentalStatus) {
    return switch (rentalStatus) {
      case ACTIVE, PENDING -> "rented";
      case COMPLETED, CANCELLED -> "available";
    };
  }
}
