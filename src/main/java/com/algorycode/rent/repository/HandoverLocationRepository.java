package com.algorycode.rent.repository;

import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.domain.location.HandoverLocationKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HandoverLocationRepository extends JpaRepository<HandoverLocation, Long> {

  List<HandoverLocation> findByKindAndActiveTrueOrderByLineOrderAscNameAsc(HandoverLocationKind kind);

  List<HandoverLocation> findByActiveTrueOrderByKindAscLineOrderAscNameAsc();

  List<HandoverLocation> findByKindOrderByActiveDescLineOrderAscNameAsc(HandoverLocationKind kind);

  List<HandoverLocation> findAllByOrderByKindAscActiveDescLineOrderAscNameAsc();
}
