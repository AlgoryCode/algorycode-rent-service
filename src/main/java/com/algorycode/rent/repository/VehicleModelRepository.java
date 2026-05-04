package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.VehicleModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {
  Optional<VehicleModel> findFirstByOrderByIdAsc();
}
