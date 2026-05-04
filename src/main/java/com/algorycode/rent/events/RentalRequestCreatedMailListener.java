package com.algorycode.rent.events;

import com.algorycode.rent.repository.RentalRequestRepository;
import com.algorycode.rent.service.RentalRequestNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RentalRequestCreatedMailListener {

  private final RentalRequestRepository rentalRequestRepository;
  private final RentalRequestNotificationService rentalRequestNotificationService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  public void onCreated(RentalRequestCreatedMailEvent event) {
    rentalRequestRepository
        .findByIdWithVehicle(event.rentalRequestId())
        .ifPresent(rentalRequestNotificationService::notifyCreated);
  }
}
