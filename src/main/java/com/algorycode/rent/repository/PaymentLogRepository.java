package com.algorycode.rent.repository;

import com.algorycode.rent.domain.payment.PaymentLog;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

  @EntityGraph(attributePaths = {"vehicle", "rental", "rental.vehicle", "rental.options"})
  @Query("select distinct p from PaymentLog p order by p.createdAt desc")
  List<PaymentLog> findAllForListingOrderByCreatedAtDesc();

  boolean existsByReference(String reference);
}
