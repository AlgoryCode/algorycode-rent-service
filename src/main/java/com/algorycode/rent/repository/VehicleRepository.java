package com.algorycode.rent.repository;

import com.algorycode.rent.domain.vehicle.Vehicle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

  Optional<Vehicle> findByPlateIgnoreCase(String plate);

  boolean existsByPlateIgnoreCase(String plate);

  @EntityGraph(
      attributePaths = {
        "statusDefinition",
        "vehicleModel",
        "vehicleModel.brand",
        "defaultPickupHandoverLocation",
        "allowedReturnHandovers",
        "allowedReturnHandovers.handoverLocation"
      })
  List<Vehicle> findAllByDeletedFalse();

  @EntityGraph(
      attributePaths = {
        "statusDefinition",
        "vehicleModel",
        "vehicleModel.brand",
        "defaultPickupHandoverLocation",
        "allowedReturnHandovers",
        "allowedReturnHandovers.handoverLocation"
      })
  Optional<Vehicle> findByIdAndDeletedFalse(Long id);

  boolean existsByPlateIgnoreCaseAndDeletedFalse(String plate);

  boolean existsByPlateIgnoreCaseAndDeletedFalseAndIdNot(String plate, Long id);

  long countByBodyStyleCodeAndDeletedFalse(String bodyStyleCode);

  long countByFuelTypeAndDeletedFalse(String fuelType);

  long countByTransmissionTypeAndDeletedFalse(String transmissionType);

  long countByStatusDefinition_IdAndDeletedFalse(Long statusDefinitionId);

  @Query(
      value =
          """
          select
            v.id as id,
            v.plate as plate,
            v.year as year,
            lower(coalesce(vs.code, 'available')) as statusCode,
            v.external_vehicle as external,
            v.rental_daily_price as rentalDailyPrice,
            v.country_code as countryCode,
            cast(v.fe_fleet_snapshot as text) as snapshotText
          from vehicles v
          left join vehicle_statuses vs on vs.id = v.vehicle_status_id
          where v.is_deleted = false
          order by v.id desc
          """,
      nativeQuery = true)
  List<VehicleSnapshotRow> findAllSnapshotsByDeletedFalse();
}
