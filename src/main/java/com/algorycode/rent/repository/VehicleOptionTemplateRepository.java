package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.VehicleOptionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleOptionTemplateRepository extends JpaRepository<VehicleOptionTemplate, Long> {

  Optional<VehicleOptionTemplate> findByIdAndActiveTrue(Long id);

  List<VehicleOptionTemplate> findByActiveTrueOrderByLineOrderAscTitleAsc();

  List<VehicleOptionTemplate> findAllByOrderByLineOrderAscTitleAsc();
}
