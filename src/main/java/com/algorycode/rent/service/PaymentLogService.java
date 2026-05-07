package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CreatePaymentLogRequest;
import com.algorycode.rent.api.dto.PaymentLogDto;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.mapper.PaymentMapper;
import com.algorycode.rent.domain.payment.PaymentLog;
import com.algorycode.rent.domain.payment.PaymentLogStatus;
import com.algorycode.rent.domain.payment.PaymentMoneyFlow;
import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalStatus;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.repository.PaymentLogRepository;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.service.support.RentalRevenueEur;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentLogService {

  private final PaymentLogRepository paymentLogRepository;
  private final RentalRepository rentalRepository;

  @Transactional(readOnly = true)
  public List<PaymentLogDto> listAll() {
    return paymentLogRepository.findAllForListingOrderByCreatedAtDesc().stream()
        .map(PaymentMapper::toDto)
        .toList();
  }

  @Transactional
  public PaymentLogDto create(CreatePaymentLogRequest req) {
    Rental rental =
        rentalRepository
            .findDetailById(req.rentalId())
            .orElseThrow(() -> new BadRequestException("Kiralama bulunamadı."));
    if (rental.getStatus() == RentalStatus.cancelled) {
      throw new BadRequestException("İptal edilmiş kiralama için ödeme eklenemez.");
    }

    PaymentLogStatus status = req.status() != null ? req.status() : PaymentLogStatus.completed;
    PaymentMoneyFlow flow =
        status == PaymentLogStatus.refunded ? PaymentMoneyFlow.refund : req.moneyFlow();

    BigDecimal amount = req.amountTry().setScale(2, RoundingMode.HALF_UP);
    if (amount.signum() <= 0) {
      throw new BadRequestException("Tutar sıfırdan büyük olmalıdır.");
    }

    BigDecimal revenueEur = RentalRevenueEur.totalRentalRevenueEur(rental);
    Vehicle v = rental.getVehicle();
    String plate = v != null && v.getPlate() != null ? v.getPlate() : "";
    String customerName =
        rental.getCustomer() != null && rental.getCustomer().getFullName() != null
            ? rental.getCustomer().getFullName()
            : "";

    PaymentLog p = new PaymentLog();
    p.setAmountTry(amount);
    p.setMoneyFlow(flow);
    p.setStatus(status);
    p.setMethod(req.method().trim());
    p.setPlate(plate);
    p.setVehicleId(rental.getVehicleId());
    p.setRental(rental);
    p.setRentalRevenueEur(revenueEur);
    p.setCustomerName(customerName);
    p.setReference(uniqueReference());
    p.setNote(req.note());

    paymentLogRepository.save(p);
    return PaymentMapper.toDto(p);
  }

  private String uniqueReference() {
    for (int i = 0; i < 8; i++) {
      String ref = "PY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
      if (!paymentLogRepository.existsByReference(ref)) {
        return ref;
      }
    }
    return "PY-" + UUID.randomUUID();
  }
}
