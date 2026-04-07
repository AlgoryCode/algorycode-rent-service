package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.PaymentLogDto;
import com.algorycode.rent.api.mapper.PaymentMapper;
import com.algorycode.rent.repository.PaymentLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PaymentLogService {

  private final PaymentLogRepository paymentLogRepository;

  public PaymentLogService(PaymentLogRepository paymentLogRepository) {
    this.paymentLogRepository = paymentLogRepository;
  }

  public List<PaymentLogDto> listAll() {
    return paymentLogRepository.findAll().stream().map(PaymentMapper::toDto).toList();
  }
}
