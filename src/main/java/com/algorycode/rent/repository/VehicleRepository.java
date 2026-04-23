package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.Vehicle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

  Optional<Vehicle> findByPlateIgnoreCase(String plate);

  boolean existsByPlateIgnoreCase(String plate);

  @EntityGraph(
      attributePaths = {
        "defaultPickupHandoverLocation",
        "allowedReturnHandovers",
        "allowedReturnHandovers.handoverLocation"
      })
  List<Vehicle> findAllByDeletedFalse();

  @EntityGraph(
      attributePaths = {
        "defaultPickupHandoverLocation",
        "allowedReturnHandovers",
        "allowedReturnHandovers.handoverLocation"
      })
  Optional<Vehicle> findByIdAndDeletedFalse(Long id);

  boolean existsByPlateIgnoreCaseAndDeletedFalse(String plate);

  boolean existsByPlateIgnoreCaseAndDeletedFalseAndIdNot(String plate, Long id);

  long countByBodyStyleCodeAndDeletedFalse(String bodyStyleCode);

  long countByFuelTypeAndDeletedFalse(String fuelType);

  long countByTransmissionTypeAndDeletedFalse(String transmissionType);
}
