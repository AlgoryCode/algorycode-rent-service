package com.algorycode.rent.controller;

import com.algorycode.rent.dto.CreateReservationExtraOptionTemplateRequest;
import com.algorycode.rent.dto.ReservationExtraOptionTemplateDto;
import com.algorycode.rent.dto.UpdateReservationExtraOptionTemplateRequest;
import com.algorycode.rent.service.ReservationExtraOptionTemplateService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation-extra-options")
@RequiredArgsConstructor
public class ReservationExtraOptionTemplateController {

  private final ReservationExtraOptionTemplateService service;

  @GetMapping
  public List<ReservationExtraOptionTemplateDto> list(
      @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
    return service.list(includeInactive);
  }

  @PostMapping
  public ReservationExtraOptionTemplateDto create(
      @Valid @RequestBody CreateReservationExtraOptionTemplateRequest body) {
    return service.create(body);
  }

  @PatchMapping("/{id}")
  public ReservationExtraOptionTemplateDto update(
      @PathVariable Long id, @Valid @RequestBody UpdateReservationExtraOptionTemplateRequest body) {
    return service.update(id, body);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deactivate(@PathVariable Long id) {
    service.deactivate(id);
  }
}
