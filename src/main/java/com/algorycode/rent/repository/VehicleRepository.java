package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.Vehicle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

  Optional<Vehicle> findByPlateIgnoreCase(String plate);

  boolean existsByPlateIgnoreCase(String plate);

  @EntityGraph(
      attributePaths = {
        "city",
        "city.country",
        "defaultPickupHandoverLocation",
        "allowedReturnHandovers",
        "allowedReturnHandovers.handoverLocation",
        "allowedReturnHandovers.handoverLocation.city",
        "allowedReturnHandovers.handoverLocation.city.country"
      })
  List<Vehicle> findAllByDeletedFalse();

  @EntityGraph(
      attributePaths = {
        "city",
        "city.country",
        "defaultPickupHandoverLocation",
        "allowedReturnHandovers",
        "allowedReturnHandovers.handoverLocation",
        "allowedReturnHandovers.handoverLocation.city",
        "allowedReturnHandovers.handoverLocation.city.country"
      })
  Optional<Vehicle> findByIdAndDeletedFalse(UUID id);

  boolean existsByPlateIgnoreCaseAndDeletedFalse(String plate);

  boolean existsByPlateIgnoreCaseAndDeletedFalseAndIdNot(String plate, UUID id);

  long countByBodyStyleCodeAndDeletedFalse(String bodyStyleCode);

  long countByFuelTypeAndDeletedFalse(String fuelType);

  long countByTransmissionTypeAndDeletedFalse(String transmissionType);
}
