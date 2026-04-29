package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.VehicleStatusDefinition;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleStatusDefinitionRepository extends JpaRepository<VehicleStatusDefinition, Long> {

  Optional<VehicleStatusDefinition> findByCodeIgnoreCase(String code);
}
