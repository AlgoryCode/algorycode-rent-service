package com.algorycode.rent.repository;

import com.algorycode.rent.domain.catalog.ReservationExtraOptionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationExtraOptionTemplateRepository
    extends JpaRepository<ReservationExtraOptionTemplate, Long> {

  List<ReservationExtraOptionTemplate> findByActiveTrueOrderByLineOrderAscTitleAsc();

  List<ReservationExtraOptionTemplate> findAllByOrderByLineOrderAscTitleAsc();

  Optional<ReservationExtraOptionTemplate> findByIdAndActiveTrue(Long id);

  boolean existsByCodeIgnoreCase(String code);

  boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
