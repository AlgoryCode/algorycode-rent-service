package com.algorycode.rent.service;

import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.messaging.MailNotificationPublisher;
import com.algorycode.rent.service.mail.RentalRequestContractCustomerMailComposer;
import com.algorycode.rent.service.mail.RentalRequestReceivedMailComposer;
import com.algorycode.rent.service.mail.RentalRequestStatusMailComposer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalRequestNotificationService {

  private final MailNotificationPublisher mailNotificationPublisher;
  private final RentalRequestReceivedMailComposer rentalRequestReceivedMailComposer;
  private final RentalRequestStatusMailComposer rentalRequestStatusMailComposer;
  private final RentalRequestContractCustomerMailComposer rentalRequestContractCustomerMailComposer;

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

  /** Sözleşme PDF müşteri e-postası (Rabbit → mail-service). */
  public void notifyContractPdfToCustomer(
      RentalRequest request, Function<String, String> contractPathResolver) {
    String email = request.getCustomer().getEmail();
    if (email == null || email.isBlank()) {
      return;
    }
    String rawPath = request.getContractPdfPath();
    String publicUrl = rawPath != null ? contractPathResolver.apply(rawPath) : "";
    mailNotificationPublisher.publish(
        rentalRequestContractCustomerMailComposer.compose(
            request, publicUrl != null ? publicUrl : ""));
  }
}
