package com.algorycode.rent.repository;

import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.domain.request.RentalRequestStatus;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RentalRequestRepository extends JpaRepository<RentalRequest, Long> {

  @EntityGraph(attributePaths = {"vehicle"})
  List<RentalRequest> findByVehicle_IdOrderByCreatedAtDesc(Long vehicleId);

  /**
   * Takvim doluluğu: reddedilmemiş talepler, [from, to] ile kesişen (uçlar dahil) kayıtlar.
   */
  @Query(
      """
      select rr from RentalRequest rr join rr.vehicle v
      where v.id = :vehicleId
        and v.deleted = false
        and rr.status in (:blockingStatuses)
        and rr.endDate >= :from
        and rr.startDate <= :to
      order by rr.startDate asc, rr.endDate asc
      """)
  List<RentalRequest> findBlockingForVehicleCalendar(
      @Param("vehicleId") Long vehicleId,
      @Param("from") LocalDate from,
      @Param("to") LocalDate to,
      @Param("blockingStatuses") List<RentalRequestStatus> blockingStatuses);

  /**
   * Filo uygunluk listesi: [from, toOrDayAfter] ile kesişebilecek pending/approved talepler (tüm
   * araçlar; {@code Rental} tampon penceresi ile aynı üst sınır).
   */
  @EntityGraph(attributePaths = {"vehicle"})
  @Query(
      """
      select rr from RentalRequest rr join rr.vehicle v
      where v.deleted = false
        and rr.status in (:blockingStatuses)
        and rr.endDate >= :from
        and rr.startDate <= :toOrDayAfter
      """)
  List<RentalRequest> findPotentiallyBlockingRequestsForAvailability(
      @Param("from") LocalDate from,
      @Param("toOrDayAfter") LocalDate toOrDayAfter,
      @Param("blockingStatuses") List<RentalRequestStatus> blockingStatuses);

  boolean existsByReferenceNo(String referenceNo);

  @EntityGraph(attributePaths = {"vehicle", "additionalDrivers"})
  Optional<RentalRequest> findByReferenceNoIgnoreCase(String referenceNo);

  @EntityGraph(attributePaths = {"vehicle"})
  @Query("select r from RentalRequest r where r.id = :id")
  Optional<RentalRequest> findByIdWithVehicle(@Param("id") Long id);

  @Query(
      """
      select rr.id from RentalRequest rr where
      (trim(coalesce(rr.customer.nationalId, '')) <> '' and concat('tc:', trim(rr.customer.nationalId)) = :recordKey)
      or (trim(coalesce(rr.customer.nationalId, '')) = '' and concat('ph:', trim(coalesce(rr.customer.phone, ''))) = :recordKey)
      """)
  List<Long> findIdsByCustomerRecordKey(@Param("recordKey") String recordKey);
}
