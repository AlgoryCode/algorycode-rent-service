package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {
  Optional<VehicleModel> findFirstByOrderByIdAsc();
}
