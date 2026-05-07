package com.algorycode.rent.api.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.algorycode.rent.api.dto.VehicleCalendarOccupancyDto;
import com.algorycode.rent.api.dto.VehicleDto;
import com.algorycode.rent.api.dto.VehicleOccupancyRangeDto;
import com.algorycode.rent.api.dto.VehicleOccupancySource;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.vehicle.VehicleStatus;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.service.VehicleFormCatalogService;
import com.algorycode.rent.service.VehicleOccupancyService;
import com.algorycode.rent.service.VehicleService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

  @Mock private VehicleService vehicleService;
  @Mock private VehicleOccupancyService vehicleOccupancyService;
  @Mock private VehicleFormCatalogService vehicleFormCatalogService;
  @Mock private AuditLog auditLog;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new VehicleController(
                    vehicleService, vehicleOccupancyService, vehicleFormCatalogService))
            .setControllerAdvice(new GlobalExceptionHandler(auditLog))
            .build();
  }

  @Test
  void list_returnsJsonArray() throws Exception {
    var id = 1L;
    when(vehicleService.listAll())
        .thenReturn(
            List.of(
                new VehicleDto(
                    id,
                    9L,
                    1L,
                    null,
                    null,
                    null,
                    "34 A 1",
                    "Toyota",
                    "Corolla",
                    2023,
                    VehicleStatus.active,
                    "active",
                    false,
                    null,
                    null,
                    false,
                    null,
                    null,
                    null,
                    "TR",
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
    var vid = 1L;
    var rid = 1L;
    when(vehicleOccupancyService.occupancy(
            vid, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30)))
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
  void create_returns201WithPlainTextMessage() throws Exception {
    when(vehicleService.create(any())).thenReturn(42L);

    mockMvc
        .perform(
            post("/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"plate":"34 X 1","vehicleModelId":5,"year":2024,"countryCode":"TR"}
                    """))
        .andExpect(status().isCreated())
        .andExpect(content().string("Vehicle created successfully"));
  }

  @Test
  void create_withoutVehicleModelId_returns201WithPlainTextMessage() throws Exception {
    when(vehicleService.create(any())).thenReturn(52L);

    mockMvc
        .perform(
            post("/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"plate":"34 X 2","year":2024,"countryCode":"TR"}
                    """))
        .andExpect(status().isCreated())
        .andExpect(content().string("Vehicle created successfully"));
  }

  @Test
  void update_returns200WithPlainTextMessage() throws Exception {
    doNothing().when(vehicleService).update(anyLong(), any());

    mockMvc
        .perform(
            patch("/vehicles/{id}", 7L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"plate":"34 X 9","vehicleStatusId":1}
                    """))
        .andExpect(status().isOk())
        .andExpect(content().string("Updated Successfully"));
  }

  @Test
  void getById_notFound_returnsProblemDetail() throws Exception {
    var id = 1L;
    when(vehicleService.getById(id))
        .thenThrow(new ResourceNotFoundException("Vehicle not found: " + id));

    mockMvc
        .perform(get("/vehicles/{id}", id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Not Found"));
  }
}
