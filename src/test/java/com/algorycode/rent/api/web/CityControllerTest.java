package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.CityDto;
import com.algorycode.rent.service.CityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CityControllerTest {

  @Mock private CityService cityService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new CityController(cityService)).build();
  }

  @Test
  void list_returnsCities() throws Exception {
    var countryId = 1L;
    var cityId = 1L;
    when(cityService.listAll(isNull()))
        .thenReturn(List.of(new CityDto(cityId, "İstanbul", countryId, "TR", "Türkiye")));

    mockMvc
        .perform(get("/cities"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("İstanbul"))
        .andExpect(jsonPath("$[0].countryCode").value("TR"));
  }

  @Test
  void create_returnsCity() throws Exception {
    var countryId = 1L;
    var cityId = 1L;
    when(cityService.create(any()))
        .thenReturn(new CityDto(cityId, "Atina", countryId, "GR", "Yunanistan"));

    mockMvc
        .perform(
            post("/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Atina\",\"countryId\":\"" + countryId + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(cityId))
        .andExpect(jsonPath("$.countryCode").value("GR"));
  }
}
