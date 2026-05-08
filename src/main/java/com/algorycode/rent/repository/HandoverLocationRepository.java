package com.algorycode.rent.repository;

import com.algorycode.rent.entity.HandoverLocation;
import com.algorycode.rent.entity.HandoverLocationKind;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HandoverLocationRepository extends JpaRepository<HandoverLocation, Long> {

  List<HandoverLocation> findByKindAndActiveTrueOrderByLineOrderAscNameAsc(
      HandoverLocationKind kind);

  List<HandoverLocation> findByActiveTrueOrderByKindAscLineOrderAscNameAsc();

  List<HandoverLocation> findByKindOrderByActiveDescLineOrderAscNameAsc(HandoverLocationKind kind);

  List<HandoverLocation> findAllByOrderByKindAscActiveDescLineOrderAscNameAsc();
}
