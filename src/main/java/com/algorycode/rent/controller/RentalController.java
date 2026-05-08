package com.algorycode.rent.controller;

import com.algorycode.rent.dto.CreateRentalRequest;
import com.algorycode.rent.dto.RentalDto;
import com.algorycode.rent.dto.UpdateRentalRequest;
import com.algorycode.rent.dto.UpdateRentalStatusRequest;
import com.algorycode.rent.service.RentalService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {

  private final RentalService rentalService;

  @GetMapping
  public List<RentalDto> list(
      @RequestParam(required = false) Long vehicleId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate) {
    return rentalService.list(vehicleId, status, startDate, endDate);
  }

  @GetMapping("/{id}")
  public RentalDto get(@PathVariable Long id) {
    return rentalService.getById(id);
  }

  @PostMapping
  public RentalDto create(@Valid @RequestBody CreateRentalRequest body) {
    return rentalService.create(body);
  }

  @PatchMapping("/{id}")
  public RentalDto update(@PathVariable Long id, @Valid @RequestBody UpdateRentalRequest body) {
    return rentalService.update(id, body);
  }

  @PatchMapping("/{id}/status")
  public RentalDto updateStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateRentalStatusRequest body) {
    return rentalService.updateStatus(id, body.status());
  }
}
