package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.PaymentLogDto;
import com.algorycode.rent.service.PaymentLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentLogController {

  private final PaymentLogService paymentLogService;

  public PaymentLogController(PaymentLogService paymentLogService) {
    this.paymentLogService = paymentLogService;
  }

  @GetMapping
  public List<PaymentLogDto> list() {
    return paymentLogService.listAll();
  }
}
