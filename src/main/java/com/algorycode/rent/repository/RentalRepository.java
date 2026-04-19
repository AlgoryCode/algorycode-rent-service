package com.algorycode.rent.repository;

import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

  @EntityGraph(attributePaths = {"vehicle", "options"})
  @Query("select r from Rental r where r.id = :id")
  Optional<Rental> findByIdWithVehicleAndOptions(@Param("id") UUID id);

  @Query(
      """
      select r.id from Rental r where
      (trim(coalesce(r.customer.nationalId, '')) <> '' and concat('tc:', trim(r.customer.nationalId)) = :recordKey)
      or (trim(coalesce(r.customer.nationalId, '')) = '' and concat('ph:', trim(coalesce(r.customer.phone, ''))) = :recordKey)
      """)
  List<UUID> findIdsByCustomerRecordKey(@Param("recordKey") String recordKey);

  /**
   * Rapor: iptal haric, tarih araligiyla kesisen kiralamalar; gelir gunluk fiyat * gun + opsiyonlardan.
   */
  @EntityGraph(attributePaths = {"vehicle", "options"})
  @Query(
      """
      select distinct r from Rental r
      join fetch r.vehicle v
      left join fetch r.options
      where r.status <> 'cancelled'
        and r.endDate >= :from
        and r.startDate <= :to
        and (:vehicleId is null or v.id = :vehicleId)
      """)
  List<Rental> findForRevenueReport(
      @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("vehicleId") UUID vehicleId);

  /**
   * Araç uygunluk listesi: [from, to] veya to+1 günüyle kesişebilecek iptal olmayan kiralamalar
   * (from/to dahil çakışma + bitiş ertesi gün tamponu).
   */
  @EntityGraph(attributePaths = {"vehicle"})
  @Query(
      """
      select r from Rental r join r.vehicle v
      where r.status <> 'cancelled'
        and v.deleted = false
        and r.endDate >= :from
        and r.startDate <= :toOrDayAfter
      """)
  List<Rental> findPotentiallyBlockingForAvailability(
      @Param("from") LocalDate from, @Param("toOrDayAfter") LocalDate toOrDayAfter);

  /**
   * Araç takvim doluluğu: iptal olmayan kiralamalar, [from, to] ile kesişen (uçlar dahil).
   */
  @Query(
      """
      select r from Rental r join r.vehicle v
      where v.id = :vehicleId
        and v.deleted = false
        and r.status <> 'cancelled'
        and r.endDate >= :from
        and r.startDate <= :to
      order by r.startDate asc, r.endDate asc
      """)
  List<Rental> findCalendarBlockingRentals(
      @Param("vehicleId") UUID vehicleId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
