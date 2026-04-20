package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.HandoverPricingQuoteDto;
import com.algorycode.rent.service.HandoverPricingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/handover-pricing")
public class HandoverPricingController {

  private final HandoverPricingService handoverPricingService;

  public HandoverPricingController(HandoverPricingService handoverPricingService) {
    this.handoverPricingService = handoverPricingService;
  }

  @GetMapping("/quote")
  public HandoverPricingQuoteDto quote(
      @RequestParam Long pickupHandoverId, @RequestParam Long returnHandoverId) {
    return handoverPricingService.quote(pickupHandoverId, returnHandoverId);
  }
}
