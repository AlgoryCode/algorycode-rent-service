package com.algorycode.rent.service;

import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.messaging.MailNotificationPublisher;
import com.algorycode.rent.service.mail.RentalRequestReceivedMailComposer;
import com.algorycode.rent.service.mail.RentalRequestStatusMailComposer;
import org.springframework.stereotype.Service;

@Service
public class RentalRequestNotificationService {

  private final MailNotificationPublisher mailNotificationPublisher;
  private final RentalRequestReceivedMailComposer rentalRequestReceivedMailComposer;
  private final RentalRequestStatusMailComposer rentalRequestStatusMailComposer;

  public RentalRequestNotificationService(
      MailNotificationPublisher mailNotificationPublisher,
      RentalRequestReceivedMailComposer rentalRequestReceivedMailComposer,
      RentalRequestStatusMailComposer rentalRequestStatusMailComposer) {
    this.mailNotificationPublisher = mailNotificationPublisher;
    this.rentalRequestReceivedMailComposer = rentalRequestReceivedMailComposer;
    this.rentalRequestStatusMailComposer = rentalRequestStatusMailComposer;
  }

  public void notifyCreated(RentalRequest request) {
    String email = request.getCustomer().getEmail();
    if (email == null || email.isBlank()) {
      return;
    }
    mailNotificationPublisher.publish(rentalRequestReceivedMailComposer.compose(request));
  }

  public void notifyStatusChanged(RentalRequest request) {
    String email = request.getCustomer().getEmail();
    if (email == null || email.isBlank()) {
      return;
    }
    mailNotificationPublisher.publish(rentalRequestStatusMailComposer.compose(request));
  }
}
