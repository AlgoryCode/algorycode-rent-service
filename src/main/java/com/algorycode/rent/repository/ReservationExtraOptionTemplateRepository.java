package com.algorycode.rent.repository;

import com.algorycode.rent.domain.catalog.ReservationExtraOptionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationExtraOptionTemplateRepository
    extends JpaRepository<ReservationExtraOptionTemplate, UUID> {

  List<ReservationExtraOptionTemplate> findByActiveTrueOrderByLineOrderAscTitleAsc();

  List<ReservationExtraOptionTemplate> findAllByOrderByLineOrderAscTitleAsc();

  Optional<ReservationExtraOptionTemplate> findByIdAndActiveTrue(UUID id);

  boolean existsByCodeIgnoreCase(String code);

  boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);
}
