package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.VehicleTransmissionType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleTransmissionTypeRepository extends JpaRepository<VehicleTransmissionType, Long> {

  List<VehicleTransmissionType> findAllByOrderBySortOrderAsc();

  @Query("select t from VehicleTransmissionType t where lower(t.code) = lower(:code)")
  Optional<VehicleTransmissionType> findByCodeIgnoreCase(@Param("code") String code);
}
