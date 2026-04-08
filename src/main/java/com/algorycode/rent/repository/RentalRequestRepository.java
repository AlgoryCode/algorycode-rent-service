package com.algorycode.rent.repository;

import com.algorycode.rent.domain.request.RentalRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RentalRequestRepository extends JpaRepository<RentalRequest, UUID> {

  boolean existsByReferenceNo(String referenceNo);

  @EntityGraph(attributePaths = {"vehicle", "additionalDrivers"})
  Optional<RentalRequest> findByReferenceNoIgnoreCase(String referenceNo);
}
