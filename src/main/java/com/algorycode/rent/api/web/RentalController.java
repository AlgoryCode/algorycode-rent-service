package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.CreateRentalRequest;
import com.algorycode.rent.api.dto.RentalDto;
import com.algorycode.rent.domain.rental.RentalStatus;
import com.algorycode.rent.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rentals")
public class RentalController {

  private final RentalService rentalService;

  public RentalController(RentalService rentalService) {
    this.rentalService = rentalService;
  }

  @GetMapping
  public List<RentalDto> list(
      @RequestParam(required = false) UUID vehicleId,
      @RequestParam(required = false) RentalStatus status) {
    return rentalService.list(vehicleId, status);
  }

  @GetMapping("/{id}")
  public RentalDto get(@PathVariable UUID id) {
    return rentalService.getById(id);
  }

  @PostMapping
  public RentalDto create(@Valid @RequestBody CreateRentalRequest body) {
    return rentalService.create(body);
  }
}
