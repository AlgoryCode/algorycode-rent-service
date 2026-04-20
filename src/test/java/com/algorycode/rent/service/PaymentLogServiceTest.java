package com.algorycode.rent.service;

import com.algorycode.rent.domain.payment.PaymentLog;
import com.algorycode.rent.domain.payment.PaymentLogStatus;
import com.algorycode.rent.domain.payment.PaymentMoneyFlow;
import com.algorycode.rent.repository.PaymentLogRepository;
import com.algorycode.rent.repository.RentalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentLogServiceTest {

  @Mock private PaymentLogRepository paymentLogRepository;

  @Mock private RentalRepository rentalRepository;

  @InjectMocks private PaymentLogService paymentLogService;

  @Test
  void listAll_returnsMappedDtos() {
    var p = new PaymentLog();
    p.setId(1L);
    p.setAmountTry(new BigDecimal("99.50"));
    p.setMoneyFlow(PaymentMoneyFlow.inbound);
    p.setStatus(PaymentLogStatus.completed);
    p.setMethod("card");
    p.setPlate("34 A 1");
    p.setCustomerName("Ali");
    p.setReference("REF-1");
    p.setNote(null);
    p.setCreatedAt(Instant.parse("2026-04-01T12:00:00Z"));
    p.setUpdatedAt(Instant.parse("2026-04-01T12:00:00Z"));
    when(paymentLogRepository.findAllForListingOrderByCreatedAtDesc()).thenReturn(List.of(p));

    var rows = paymentLogService.listAll();

    assertThat(rows).hasSize(1);
    assertThat(rows.getFirst().reference()).isEqualTo("REF-1");
    assertThat(rows.getFirst().amountTry()).isEqualByComparingTo("99.50");
  }
}
