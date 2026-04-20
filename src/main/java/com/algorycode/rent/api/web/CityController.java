package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.CityDto;
import com.algorycode.rent.api.dto.CreateCityRequest;
import com.algorycode.rent.service.CityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cities")
public class CityController {

  private final CityService cityService;

  public CityController(CityService cityService) {
    this.cityService = cityService;
  }

  @GetMapping
  public List<CityDto> list(@RequestParam(required = false) Long countryId) {
    return cityService.listAll(countryId);
  }

  @PostMapping
  public CityDto create(@Valid @RequestBody CreateCityRequest body) {
    return cityService.create(body);
  }
}
