package com.algorycode.rent.repository;

import com.algorycode.rent.domain.customer.CustomerRecordState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRecordStateRepository extends JpaRepository<CustomerRecordState, String> {}
