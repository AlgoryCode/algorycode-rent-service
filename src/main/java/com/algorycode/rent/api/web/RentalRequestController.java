package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.CreateRentalRequestFormRequest;
import com.algorycode.rent.api.dto.RentalRequestDto;
import com.algorycode.rent.api.dto.UpdateRentalRequestStatusRequest;
import com.algorycode.rent.service.RentalRequestService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rental-requests")
public class RentalRequestController {

  private final RentalRequestService rentalRequestService;

  public RentalRequestController(RentalRequestService rentalRequestService) {
    this.rentalRequestService = rentalRequestService;
  }

  @GetMapping
  public List<RentalRequestDto> list() {
    return rentalRequestService.listAll();
  }

  @PostMapping
  public RentalRequestDto create(@Valid @RequestBody CreateRentalRequestFormRequest body) {
    return rentalRequestService.create(body);
  }

  @GetMapping("/reference/{referenceNo}")
  public RentalRequestDto getByReference(@PathVariable String referenceNo) {
    return rentalRequestService.getByReferenceNo(referenceNo);
  }

  @PatchMapping("/{id}/status")
  public RentalRequestDto updateStatus(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateRentalRequestStatusRequest body) {
    return rentalRequestService.updateStatus(id, body);
  }
}
