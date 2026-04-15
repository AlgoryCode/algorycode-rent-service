package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.VehicleOptionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleOptionTemplateRepository extends JpaRepository<VehicleOptionTemplate, UUID> {

  Optional<VehicleOptionTemplate> findByIdAndActiveTrue(UUID id);

  List<VehicleOptionTemplate> findByActiveTrueOrderByLineOrderAscTitleAsc();

  List<VehicleOptionTemplate> findAllByOrderByLineOrderAscTitleAsc();
}
