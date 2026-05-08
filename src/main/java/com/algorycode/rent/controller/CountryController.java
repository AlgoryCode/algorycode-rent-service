package com.algorycode.rent.controller;

import com.algorycode.rent.dto.CountryDto;
import com.algorycode.rent.dto.CreateCountryRequest;
import com.algorycode.rent.dto.UpdateCountryColorRequest;
import com.algorycode.rent.service.CountryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
public class CountryController {

  private final CountryService countryService;

  @GetMapping
  public List<CountryDto> list() {
    return countryService.listAll();
  }

  @PostMapping
  public CountryDto create(@Valid @RequestBody CreateCountryRequest body) {
    return countryService.create(body);
  }

  @PatchMapping("/{id}")
  public CountryDto updateColor(
      @PathVariable Long id, @Valid @RequestBody UpdateCountryColorRequest body) {
    return countryService.updateColor(id, body);
  }
}
