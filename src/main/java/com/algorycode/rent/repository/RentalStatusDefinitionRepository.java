package com.algorycode.rent.repository;

import com.algorycode.rent.entity.RentalStatusDefinition;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalStatusDefinitionRepository
    extends JpaRepository<RentalStatusDefinition, Long> {

  Optional<RentalStatusDefinition> findByCodeIgnoreCase(String code);
}
