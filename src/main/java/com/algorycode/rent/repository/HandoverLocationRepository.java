package com.algorycode.rent.repository;

import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.domain.location.HandoverLocationKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HandoverLocationRepository extends JpaRepository<HandoverLocation, UUID> {

  @Query(
      "select h from HandoverLocation h left join fetch h.city c left join fetch c.country where h.id = :id")
  Optional<HandoverLocation> findByIdWithCityAndCountry(@Param("id") UUID id);

  List<HandoverLocation> findByKindAndActiveTrueOrderByLineOrderAscNameAsc(HandoverLocationKind kind);

  List<HandoverLocation> findByActiveTrueOrderByKindAscLineOrderAscNameAsc();

  List<HandoverLocation> findByKindOrderByActiveDescLineOrderAscNameAsc(HandoverLocationKind kind);

  List<HandoverLocation> findAllByOrderByKindAscActiveDescLineOrderAscNameAsc();
}
