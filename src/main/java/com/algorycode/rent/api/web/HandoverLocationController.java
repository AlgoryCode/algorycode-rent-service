package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.CreateHandoverLocationRequest;
import com.algorycode.rent.api.dto.HandoverLocationDto;
import com.algorycode.rent.api.dto.UpdateHandoverLocationRequest;
import com.algorycode.rent.domain.location.HandoverLocationKind;
import com.algorycode.rent.service.HandoverLocationService;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/handover-locations")
public class HandoverLocationController {

  private final HandoverLocationService handoverLocationService;

  public HandoverLocationController(HandoverLocationService handoverLocationService) {
    this.handoverLocationService = handoverLocationService;
  }

  @GetMapping
  public List<HandoverLocationDto> list(
      @RequestParam(required = false) HandoverLocationKind kind,
      @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
    return handoverLocationService.list(kind, includeInactive);
  }

  @PostMapping
  public HandoverLocationDto create(@Valid @RequestBody CreateHandoverLocationRequest body) {
    return handoverLocationService.create(body);
  }

  @PatchMapping("/{id}")
  public HandoverLocationDto update(
      @PathVariable UUID id, @Valid @RequestBody UpdateHandoverLocationRequest body) {
    return handoverLocationService.update(id, body);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deactivate(@PathVariable UUID id) {
    handoverLocationService.deactivate(id);
  }
}
