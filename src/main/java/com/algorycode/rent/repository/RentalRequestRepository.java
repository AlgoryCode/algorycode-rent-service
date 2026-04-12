package com.algorycode.rent.repository;

import com.algorycode.rent.domain.request.RentalRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RentalRequestRepository extends JpaRepository<RentalRequest, UUID> {

  boolean existsByReferenceNo(String referenceNo);

  @EntityGraph(attributePaths = {"vehicle", "additionalDrivers"})
  Optional<RentalRequest> findByReferenceNoIgnoreCase(String referenceNo);

  @Query(
      """
      select rr.id from RentalRequest rr where
      (trim(coalesce(rr.customer.nationalId, '')) <> '' and concat('tc:', trim(rr.customer.nationalId)) = :recordKey)
      or (trim(coalesce(rr.customer.nationalId, '')) = '' and concat('ph:', trim(coalesce(rr.customer.phone, ''))) = :recordKey)
      """)
  List<UUID> findIdsByCustomerRecordKey(@Param("recordKey") String recordKey);
}
