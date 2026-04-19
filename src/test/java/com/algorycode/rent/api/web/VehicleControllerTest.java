package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.VehicleCalendarOccupancyDto;
import com.algorycode.rent.api.dto.VehicleDto;
import com.algorycode.rent.api.dto.VehicleOccupancyRangeDto;
import com.algorycode.rent.api.dto.VehicleOccupancySource;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.service.VehicleOccupancyService;
import com.algorycode.rent.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

  @Mock private VehicleService vehicleService;
  @Mock private VehicleOccupancyService vehicleOccupancyService;
  @Mock private AuditLog auditLog;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new VehicleController(vehicleService, vehicleOccupancyService))
            .setControllerAdvice(new GlobalExceptionHandler(auditLog))
            .build();
  }

  @Test
  void list_returnsJsonArray() throws Exception {
    var id = UUID.randomUUID();
    when(vehicleService.listAll())
        .thenReturn(
            List.of(new VehicleDto(
                id,
                "34 A 1",
                "Toyota",
                "Corolla",
                2023,
                false,
                false,
                null,
                null,
                false,
                null,
                null,
                null,
                "TR",
                "Türkiye",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Map.of())));

    mockMvc
        .perform(get("/vehicles").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].plate").value("34 A 1"))
        .andExpect(jsonPath("$[0].brand").value("Toyota"));
  }

  @Test
  void calendarOccupancy_returnsMergedRanges() throws Exception {
    var vid = UUID.randomUUID();
    var rid = UUID.randomUUID();
    when(vehicleOccupancyService.occupancy(vid, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30)))
        .thenReturn(
            new VehicleCalendarOccupancyDto(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                List.of(
                    new VehicleOccupancyRangeDto(
                        rid,
                        VehicleOccupancySource.rental,
                        LocalDate.of(2026, 4, 19),
                        LocalDate.of(2026, 4, 21)))));

    mockMvc
        .perform(
            get("/vehicles/{id}/calendar/occupancy", vid)
                .param("from", "2026-04-01")
                .param("to", "2026-04-30")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.from").value("2026-04-01"))
        .andExpect(jsonPath("$.to").value("2026-04-30"))
        .andExpect(jsonPath("$.ranges[0].startDate").value("2026-04-19"))
        .andExpect(jsonPath("$.ranges[0].endDate").value("2026-04-21"))
        .andExpect(jsonPath("$.ranges[0].source").value("rental"));
  }

  @Test
  void getById_notFound_returnsProblemDetail() throws Exception {
    var id = UUID.randomUUID();
    when(vehicleService.getById(id)).thenThrow(new ResourceNotFoundException("Vehicle not found: " + id));

    mockMvc
        .perform(get("/vehicles/{id}", id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Not Found"));
  }
}
