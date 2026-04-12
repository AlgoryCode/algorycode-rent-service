package com.algorycode.rent.repository;

import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RentalRepository extends JpaRepository<Rental, UUID>, JpaSpecificationExecutor<Rental> {

  @EntityGraph(attributePaths = {"vehicle"})
  List<Rental> findAllByOrderByCreatedAtDesc();

  @EntityGraph(attributePaths = {"vehicle"})
  List<Rental> findByStatusOrderByCreatedAtDesc(RentalStatus status);

  @EntityGraph(attributePaths = {"vehicle"})
  List<Rental> findByVehicle_IdOrderByCreatedAtDesc(UUID vehicleId);

  @EntityGraph(attributePaths = {"vehicle"})
  List<Rental> findByVehicle_IdAndStatusOrderByCreatedAtDesc(UUID vehicleId, RentalStatus status);

  boolean existsByVehicle_Id(UUID vehicleId);

  @Query(
      """
      select r.id from Rental r where
      (trim(coalesce(r.customer.nationalId, '')) <> '' and concat('tc:', trim(r.customer.nationalId)) = :recordKey)
      or (trim(coalesce(r.customer.nationalId, '')) = '' and concat('ph:', trim(coalesce(r.customer.phone, ''))) = :recordKey)
      """)
  List<UUID> findIdsByCustomerRecordKey(@Param("recordKey") String recordKey);
}
