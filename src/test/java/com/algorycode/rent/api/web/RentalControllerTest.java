package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.RentalDto;
import com.algorycode.rent.domain.rental.RentalCommissionFlow;
import com.algorycode.rent.domain.rental.RentalStatus;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.service.RentalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RentalControllerTest {

  @Mock private RentalService rentalService;
  @Mock private AuditLog auditLog;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new RentalController(rentalService))
            .setControllerAdvice(new GlobalExceptionHandler(auditLog))
            .build();
  }

  @Test
  void list_withStatusQuery_passesToService() throws Exception {
    var dto =
        new RentalDto(
            1L,
            2L,
            null,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 5),
            null,
            null,
            Instant.parse("2025-12-01T10:00:00Z"),
            RentalStatus.pending,
            java.math.BigDecimal.valueOf(100),
            RentalCommissionFlow.collect,
            null,
            new RentalDto.CustomerDto("A", "1", "P", "+90", null, null, null, null, null),
            List.of(),
            null,
            List.of(),
            List.of(),
            List.of());
    when(rentalService.list(nullable(Long.class), eq(RentalStatus.pending), isNull(), isNull()))
        .thenReturn(List.of(dto));

    mockMvc
        .perform(
            get("/rentals")
                .param("status", "pending")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].status").value("pending"));
  }

  @Test
  void list_withoutParams_callsServiceWithNulls() throws Exception {
    when(rentalService.list(isNull(), isNull(), isNull(), isNull())).thenReturn(List.of());

    mockMvc.perform(get("/rentals")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
  }

  @Test
  void updateStatus_delegatesToService() throws Exception {
    var dto =
        new RentalDto(
            1L,
            2L,
            null,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 5),
            null,
            null,
            Instant.parse("2025-12-01T10:00:00Z"),
            RentalStatus.cancelled,
            java.math.BigDecimal.valueOf(100),
            RentalCommissionFlow.collect,
            null,
            new RentalDto.CustomerDto("A", "1", "P", "+90", null, null, null, null, null),
            List.of(),
            null,
            List.of(),
            List.of(),
            List.of());
    when(rentalService.updateStatus(1L, RentalStatus.cancelled)).thenReturn(dto);

    mockMvc
        .perform(
            patch("/rentals/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"cancelled\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("cancelled"));

    verify(rentalService).updateStatus(1L, RentalStatus.cancelled);
  }
}
