package com.algorycode.rent.events;

import com.algorycode.rent.repository.RentalRequestRepository;
import com.algorycode.rent.service.RentalRequestNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RentalRequestCreatedMailListener {

  private final RentalRequestRepository rentalRequestRepository;
  private final RentalRequestNotificationService rentalRequestNotificationService;

  public RentalRequestCreatedMailListener(
      RentalRequestRepository rentalRequestRepository,
      RentalRequestNotificationService rentalRequestNotificationService) {
    this.rentalRequestRepository = rentalRequestRepository;
    this.rentalRequestNotificationService = rentalRequestNotificationService;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  public void onCreated(RentalRequestCreatedMailEvent event) {
    rentalRequestRepository
        .findByIdWithVehicle(event.rentalRequestId())
        .ifPresent(rentalRequestNotificationService::notifyCreated);
  }
}
