package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.CountryDto;
import com.algorycode.rent.service.CountryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CountryControllerTest {

  @Mock private CountryService countryService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new CountryController(countryService)).build();
  }

  @Test
  void list_returnsCountries() throws Exception {
    var dto =
        new CountryDto(1L, "TR", "Türkiye", "#E30A17");
    when(countryService.listAll()).thenReturn(List.of(dto));

    mockMvc
        .perform(get("/countries"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].code").value("TR"))
        .andExpect(jsonPath("$[0].colorCode").value("#E30A17"));
  }

  @Test
  void create_returnsCountry() throws Exception {
    var id = 1L;
    var dto = new CountryDto(id, "DE", "Almanya", "#000000");
    when(countryService.create(any())).thenReturn(dto);

    mockMvc
        .perform(
            post("/countries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"de\",\"name\":\"Almanya\",\"colorCode\":\"#000000\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.code").value("DE"))
        .andExpect(jsonPath("$.name").value("Almanya"));
  }
}
