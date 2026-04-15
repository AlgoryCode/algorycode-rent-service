package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.VehicleOptionDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleOptionDefinitionRepository extends JpaRepository<VehicleOptionDefinition, UUID> {

  List<VehicleOptionDefinition> findByVehicle_IdOrderByLineOrderAscTitleAsc(UUID vehicleId);

  Optional<VehicleOptionDefinition> findByIdAndVehicle_Id(UUID id, UUID vehicleId);
}
