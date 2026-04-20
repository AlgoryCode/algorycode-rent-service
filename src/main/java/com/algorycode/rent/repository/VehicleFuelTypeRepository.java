package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.VehicleFuelType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleFuelTypeRepository extends JpaRepository<VehicleFuelType, Long> {

  List<VehicleFuelType> findAllByOrderBySortOrderAsc();

  @Query("select f from VehicleFuelType f where lower(f.code) = lower(:code)")
  Optional<VehicleFuelType> findByCodeIgnoreCase(@Param("code") String code);
}
