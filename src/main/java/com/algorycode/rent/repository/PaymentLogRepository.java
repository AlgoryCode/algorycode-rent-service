package com.algorycode.rent.repository;

import com.algorycode.rent.domain.payment.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, UUID> {}
