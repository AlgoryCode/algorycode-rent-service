package com.algorycode.rent.repository;

import com.algorycode.rent.entity.VehicleOptionTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleOptionTemplateRepository
    extends JpaRepository<VehicleOptionTemplate, Long> {

  Optional<VehicleOptionTemplate> findByIdAndActiveTrue(Long id);

  List<VehicleOptionTemplate> findByActiveTrueOrderByLineOrderAscTitleAsc();

  List<VehicleOptionTemplate> findAllByOrderByLineOrderAscTitleAsc();
}
