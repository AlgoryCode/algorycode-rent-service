package com.algorycode.rent.repository;

import com.algorycode.rent.entity.VehicleStatusCatalog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleStatusCatalogRepository extends JpaRepository<VehicleStatusCatalog, Long> {

  Optional<VehicleStatusCatalog> findByCodeIgnoreCase(String code);
}
