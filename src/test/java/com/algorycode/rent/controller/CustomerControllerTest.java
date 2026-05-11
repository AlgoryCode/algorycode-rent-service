package com.algorycode.rent.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.algorycode.rent.dto.CustomerResponse;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.service.CustomerService;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

  @Mock private CustomerService customerService;
  @Mock private AuditLog auditLog;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    PageableHandlerMethodArgumentResolver pageableResolver =
        new PageableHandlerMethodArgumentResolver();
    mockMvc =
        MockMvcBuilders.standaloneSetup(new CustomerController(customerService))
            .setCustomArgumentResolvers(pageableResolver)
            .setControllerAdvice(new GlobalExceptionHandler(auditLog))
            .build();
  }

  @Test
  void list_returnsPage() throws Exception {
    var row =
        new CustomerResponse(
            1L,
            Instant.parse("2026-01-01T10:00:00Z"),
            Instant.parse("2026-01-01T10:00:00Z"),
            "Ali",
            "",
            "",
            "+90",
            null,
            LocalDate.of(1990, 1, 1),
            null,
            null,
            null);
    when(customerService.list(any()))
        .thenReturn(new PageImpl<>(java.util.List.of(row), PageRequest.of(0, 20), 1));

    mockMvc
        .perform(get("/customers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].fullName").value("Ali"));
  }

  @Test
  void get_returnsOne() throws Exception {
    when(customerService.getById(5L))
        .thenReturn(
            new CustomerResponse(
                5L,
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T10:00:00Z"),
                "Veli",
                "1",
                "",
                "+91",
                null,
                null,
                null,
                null,
                null));

    mockMvc
        .perform(get("/customers/5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(5))
        .andExpect(jsonPath("$.fullName").value("Veli"));
  }

  @Test
  void delete_callsService() throws Exception {
    mockMvc.perform(delete("/customers/9")).andExpect(status().isNoContent());

    verify(customerService).delete(9L);
  }

  @Test
  void create_acceptsJson() throws Exception {
    when(customerService.create(any()))
        .thenAnswer(
            inv ->
                new CustomerResponse(
                    3L,
                    Instant.parse("2026-01-01T10:00:00Z"),
                    Instant.parse("2026-01-01T10:00:00Z"),
                    "Ayşe",
                    "",
                    "",
                    "+905551112233",
                    "ayse@test.com",
                    null,
                    null,
                    null,
                    null));

    mockMvc
        .perform(
            post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"fullName":"Ayşe","nationalId":"","passportNo":"","phone":"+905551112233","email":"ayse@test.com"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(3));
  }

  @Test
  void update_acceptsPatchBody() throws Exception {
    when(customerService.update(anyLong(), any()))
        .thenReturn(
            new CustomerResponse(
                2L,
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-02T10:00:00Z"),
                "Zeynep",
                "",
                "",
                "+90",
                "z@x.com",
                null,
                null,
                null,
                null));

    mockMvc
        .perform(
            patch("/customers/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"z@x.com\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("z@x.com"));
  }
}
