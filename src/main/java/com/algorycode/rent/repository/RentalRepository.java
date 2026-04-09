package com.algorycode.rent.repository;

import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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
}
