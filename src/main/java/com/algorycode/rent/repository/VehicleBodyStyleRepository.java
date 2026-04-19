package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.VehicleBodyStyle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleBodyStyleRepository extends JpaRepository<VehicleBodyStyle, String> {

  List<VehicleBodyStyle> findAllByOrderBySortOrderAsc();

  @Query("select b from VehicleBodyStyle b where lower(b.code) = lower(:code)")
  Optional<VehicleBodyStyle> findByCodeIgnoreCase(@Param("code") String code);
}
