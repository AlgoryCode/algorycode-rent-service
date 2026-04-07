package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

  Optional<Vehicle> findByPlateIgnoreCase(String plate);

  boolean existsByPlateIgnoreCase(String plate);
}
