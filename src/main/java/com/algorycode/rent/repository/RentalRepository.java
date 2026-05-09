package com.algorycode.rent.repository;

import com.algorycode.rent.entity.Rental;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalRepository
    extends JpaRepository<Rental, Long>, JpaSpecificationExecutor<Rental> {

  long countByCustomerId(Long customerId);

  @EntityGraph(attributePaths = {"vehicle", "statusDefinition", "customer"})
  List<Rental> findAllByOrderByCreatedAtDesc();

  @EntityGraph(attributePaths = {"vehicle", "statusDefinition", "customer"})
  List<Rental> findByStatusDefinition_CodeOrderByCreatedAtDesc(String code);

  @EntityGraph(attributePaths = {"vehicle", "statusDefinition", "customer"})
  List<Rental> findByVehicle_IdOrderByCreatedAtDesc(Long vehicleId);

  @EntityGraph(attributePaths = {"vehicle", "statusDefinition", "customer"})
  List<Rental> findByVehicle_IdAndStatusDefinition_CodeOrderByCreatedAtDesc(
      Long vehicleId, String code);

  boolean existsByVehicle_Id(Long vehicleId);

  boolean existsByVehicle_IdAndStatusDefinition_CodeIn(Long vehicleId, Collection<String> codes);

  boolean existsByVehicle_IdAndStatusDefinition_CodeInAndIdNot(
      Long vehicleId, Collection<String> codes, Long id);

  @EntityGraph(
      attributePaths = {
        "statusDefinition",
        "vehicle",
        "vehicle.vehicleStatus",
        "vehicle.vehicleModel",
        "vehicle.vehicleModel.brand",
        "pickupHandoverLocation",
        "returnHandoverLocation",
        "feedback",
        "customer",
        "options",
        "options.vehicleOptionDefinition",
        "options.reservationExtraTemplate"
      })
  @Query("select r from Rental r where r.id = :id")
  Optional<Rental> findDetailById(@Param("id") Long id);

  @Query(
      """
      select r.id from Rental r where
      (trim(coalesce(r.customer.nationalId, '')) <> '' and concat('tc:', trim(r.customer.nationalId)) = :recordKey)
      or (trim(coalesce(r.customer.nationalId, '')) = '' and concat('ph:', trim(coalesce(r.customer.phone, ''))) = :recordKey)
      """)
  List<Long> findIdsByCustomerRecordKey(@Param("recordKey") String recordKey);

  @EntityGraph(
      attributePaths = {
        "vehicle",
        "statusDefinition",
        "customer",
        "options",
        "options.vehicleOptionDefinition",
        "options.reservationExtraTemplate"
      })
  @Query(
      """
      select distinct r from Rental r join r.vehicle v
      where r.statusDefinition.code <> 'cancelled'
        and r.endDate >= :from
        and r.startDate <= :to
        and (:vehicleId is null or v.id = :vehicleId)
      """)
  List<Rental> findForRevenueReport(
      @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("vehicleId") Long vehicleId);

  @EntityGraph(attributePaths = {"vehicle", "statusDefinition", "customer"})
  @Query(
      """
      select r from Rental r join r.vehicle v
      where r.statusDefinition.code <> 'cancelled'
        and v.deleted = false
        and r.endDate >= :from
        and r.startDate <= :toOrDayAfter
      """)
  List<Rental> findPotentiallyBlockingForAvailability(
      @Param("from") LocalDate from, @Param("toOrDayAfter") LocalDate toOrDayAfter);

  @EntityGraph(attributePaths = {"vehicle", "statusDefinition", "customer"})
  @Query(
      """
      select r from Rental r join r.vehicle v
      where v.id = :vehicleId
        and v.deleted = false
        and r.statusDefinition.code = 'active'
        and r.endDate >= :from
        and r.startDate <= :to
      order by r.startDate asc, r.endDate asc
      """)
  List<Rental> findCalendarBlockingRentals(
      @Param("vehicleId") Long vehicleId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
