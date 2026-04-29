package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.VehicleBrand;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VehicleBrandRepository extends JpaRepository<VehicleBrand, Long> {

  @EntityGraph(attributePaths = "models")
  @Query("select b from VehicleBrand b order by b.sortOrder asc, b.name asc")
  List<VehicleBrand> findAllWithModelsForCatalog();
}
