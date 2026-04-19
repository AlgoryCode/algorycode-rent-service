package com.algorycode.rent.repository;

import com.algorycode.rent.domain.payment.PaymentLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, UUID> {

  @EntityGraph(attributePaths = {"vehicle", "rental", "rental.vehicle", "rental.options"})
  @Query("select distinct p from PaymentLog p order by p.createdAt desc")
  List<PaymentLog> findAllForListingOrderByCreatedAtDesc();

  boolean existsByReference(String reference);
}
