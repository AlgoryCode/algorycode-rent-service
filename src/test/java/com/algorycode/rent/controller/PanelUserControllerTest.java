package com.algorycode.rent.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.algorycode.rent.dto.PanelUserDto;
import com.algorycode.rent.entity.PanelUserRole;
import com.algorycode.rent.service.PanelUserService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PanelUserControllerTest {

  @Mock private PanelUserService panelUserService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new PanelUserController(panelUserService)).build();
  }

  @Test
  void list_returnsUsers() throws Exception {
    var dto =
        new PanelUserDto(
            1L,
            "User",
            "u@x.com",
            PanelUserRole.viewer,
            Instant.parse("2026-01-01T00:00:00Z"),
            true);
    when(panelUserService.listAll()).thenReturn(List.of(dto));

    mockMvc
        .perform(get("/panel-users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].email").value("u@x.com"))
        .andExpect(jsonPath("$[0].role").value("viewer"));
  }

  @Test
  void delete_returnsNoContent() throws Exception {
    var id = 1L;
    doNothing().when(panelUserService).deleteById(id);

    mockMvc.perform(delete("/panel-users/" + id)).andExpect(status().isOk());
  }
}
