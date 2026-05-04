package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.VehicleOptionDefinition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleOptionDefinitionRepository
    extends JpaRepository<VehicleOptionDefinition, Long> {

  List<VehicleOptionDefinition> findByVehicle_IdOrderByLineOrderAscTitleAsc(Long vehicleId);

  Optional<VehicleOptionDefinition> findByIdAndVehicle_Id(Long id, Long vehicleId);
}
