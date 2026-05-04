package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.CreatePaymentLogRequest;
import com.algorycode.rent.api.dto.PaymentLogDto;
import com.algorycode.rent.service.PaymentLogService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentLogController {

  private final PaymentLogService paymentLogService;

  @GetMapping
  public List<PaymentLogDto> list() {
    return paymentLogService.listAll();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PaymentLogDto create(@Valid @RequestBody CreatePaymentLogRequest body) {
    return paymentLogService.create(body);
  }
}
