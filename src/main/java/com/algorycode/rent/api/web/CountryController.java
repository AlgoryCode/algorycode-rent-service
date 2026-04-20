package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.CountryDto;
import com.algorycode.rent.api.dto.CreateCountryRequest;
import com.algorycode.rent.api.dto.UpdateCountryColorRequest;
import com.algorycode.rent.service.CountryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/countries")
public class CountryController {

  private final CountryService countryService;

  public CountryController(CountryService countryService) {
    this.countryService = countryService;
  }

  @GetMapping
  public List<CountryDto> list() {
    return countryService.listAll();
  }

  @PostMapping
  public CountryDto create(@Valid @RequestBody CreateCountryRequest body) {
    return countryService.create(body);
  }

  @PatchMapping("/{id}")
  public CountryDto updateColor(@PathVariable Long id, @Valid @RequestBody UpdateCountryColorRequest body) {
    return countryService.updateColor(id, body);
  }
}
