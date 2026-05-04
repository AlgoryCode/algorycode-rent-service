package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.Vehicle;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

  Optional<Vehicle> findByPlateIgnoreCase(String plate);

  boolean existsByPlateIgnoreCase(String plate);

  @EntityGraph(
      attributePaths = {
        "statusDefinition",
        "vehicleModel",
        "vehicleModel.brand",
        "defaultPickupHandoverLocation",
        "allowedReturnHandovers",
        "allowedReturnHandovers.handoverLocation"
      })
  List<Vehicle> findAllByDeletedFalse();

  @EntityGraph(
      attributePaths = {
        "statusDefinition",
        "vehicleModel",
        "vehicleModel.brand",
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

  long countByStatusDefinition_IdAndDeletedFalse(Long statusDefinitionId);

  @Query("select v.feFleetSnapshot from Vehicle v where v.deleted = false order by v.id desc")
  List<JsonNode> findAllFeFleetSnapshotsByDeletedFalse();
}
