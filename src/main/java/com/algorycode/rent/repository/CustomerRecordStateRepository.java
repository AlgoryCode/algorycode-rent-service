package com.algorycode.rent.repository;

import com.algorycode.rent.entity.CustomerRecordState;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRecordStateRepository extends JpaRepository<CustomerRecordState, String> {

  List<CustomerRecordState> findAllByDeletedFalseOrderByRecordKeyAsc();
}
