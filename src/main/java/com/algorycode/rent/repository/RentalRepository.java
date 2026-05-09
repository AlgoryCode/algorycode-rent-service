package com.algorycode.rent.repository;

import com.algorycode.rent.entity.Rental;
import com.algorycode.rent.entity.RentalStatus;
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

  @EntityGraph(attributePaths = {"vehicle", "customer"})
  List<Rental> findAllByOrderByCreatedAtDesc();

  @EntityGraph(attributePaths = {"vehicle", "customer"})
  List<Rental> findByRentalStatusOrderByCreatedAtDesc(RentalStatus rentalStatus);

  @EntityGraph(attributePaths = {"vehicle", "customer"})
  List<Rental> findByVehicle_IdOrderByCreatedAtDesc(Long vehicleId);

  @EntityGraph(attributePaths = {"vehicle", "customer"})
  List<Rental> findByVehicle_IdAndRentalStatusOrderByCreatedAtDesc(
      Long vehicleId, RentalStatus rentalStatus);

  boolean existsByVehicle_Id(Long vehicleId);

  boolean existsByVehicle_IdAndRentalStatusIn(Long vehicleId, Collection<RentalStatus> statuses);

  boolean existsByVehicle_IdAndRentalStatusInAndIdNot(
      Long vehicleId, Collection<RentalStatus> statuses, Long id);

  @EntityGraph(
      attributePaths = {
        "vehicle",
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
        "customer",
        "options",
        "options.vehicleOptionDefinition",
        "options.reservationExtraTemplate"
      })
  @Query(
      """
      select distinct r from Rental r join r.vehicle v
      where r.rentalStatus <> :cancelled
        and r.endDate >= :from
        and r.startDate <= :to
        and (:vehicleId is null or v.id = :vehicleId)
      """)
  List<Rental> findForRevenueReport(
      @Param("from") LocalDate from,
      @Param("to") LocalDate to,
      @Param("vehicleId") Long vehicleId,
      @Param("cancelled") RentalStatus cancelled);

  @EntityGraph(attributePaths = {"vehicle", "customer"})
  @Query(
      """
      select r from Rental r join r.vehicle v
      where r.rentalStatus <> :cancelled
        and v.deleted = false
        and r.endDate >= :from
        and r.startDate <= :toOrDayAfter
      """)
  List<Rental> findPotentiallyBlockingForAvailability(
      @Param("from") LocalDate from,
      @Param("toOrDayAfter") LocalDate toOrDayAfter,
      @Param("cancelled") RentalStatus cancelled);

  @EntityGraph(attributePaths = {"vehicle", "customer"})
  @Query(
      """
      select r from Rental r join r.vehicle v
      where v.id = :vehicleId
        and v.deleted = false
        and r.rentalStatus = :active
        and r.endDate >= :from
        and r.startDate <= :to
      order by r.startDate asc, r.endDate asc
      """)
  List<Rental> findCalendarBlockingRentals(
      @Param("vehicleId") Long vehicleId,
      @Param("from") LocalDate from,
      @Param("to") LocalDate to,
      @Param("active") RentalStatus active);
}
