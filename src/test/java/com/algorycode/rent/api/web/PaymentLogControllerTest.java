package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.PaymentLogDto;
import com.algorycode.rent.domain.payment.PaymentLogStatus;
import com.algorycode.rent.domain.payment.PaymentMoneyFlow;
import com.algorycode.rent.service.PaymentLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentLogControllerTest {

  @Mock private PaymentLogService paymentLogService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new PaymentLogController(paymentLogService)).build();
  }

  @Test
  void list_returnsPayments() throws Exception {
    var dto =
        new PaymentLogDto(
            1L,
            Instant.parse("2026-01-01T00:00:00Z"),
            new BigDecimal("100.00"),
            PaymentMoneyFlow.inbound,
            PaymentLogStatus.completed,
            "card",
            "34 A",
            1L,
            "X",
            "REF-1",
            null,
            null,
            null,
            null,
            null,
            null);
    when(paymentLogService.listAll()).thenReturn(List.of(dto));

    mockMvc
        .perform(get("/payments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].reference").value("REF-1"));
  }
}
