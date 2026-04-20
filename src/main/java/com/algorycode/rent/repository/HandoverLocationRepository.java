package com.algorycode.rent.repository;

import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.domain.location.HandoverLocationKind;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HandoverLocationRepository extends JpaRepository<HandoverLocation, Long> {

  @Query(
      "select h from HandoverLocation h left join fetch h.city c left join fetch c.country where h.id = :id")
  Optional<HandoverLocation> findByIdWithCityAndCountry(@Param("id") Long id);

  /** Liste + DTO map: {@code city} / {@code country} lazy + open-in-view kapalı iken güvenli olsun. */
  @EntityGraph(attributePaths = {"city", "city.country"})
  List<HandoverLocation> findByKindAndActiveTrueOrderByLineOrderAscNameAsc(HandoverLocationKind kind);

  @EntityGraph(attributePaths = {"city", "city.country"})
  List<HandoverLocation> findByActiveTrueOrderByKindAscLineOrderAscNameAsc();

  @EntityGraph(attributePaths = {"city", "city.country"})
  List<HandoverLocation> findByKindOrderByActiveDescLineOrderAscNameAsc(HandoverLocationKind kind);

  @EntityGraph(attributePaths = {"city", "city.country"})
  List<HandoverLocation> findAllByOrderByKindAscActiveDescLineOrderAscNameAsc();
}
