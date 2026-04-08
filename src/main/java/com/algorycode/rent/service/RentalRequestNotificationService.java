package com.algorycode.rent.service;

import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.messaging.MailNotificationPublisher;
import com.algorycode.rent.messaging.MailSendRequestedEvent;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RentalRequestNotificationService {

  private final MailNotificationPublisher mailNotificationPublisher;

  public RentalRequestNotificationService(MailNotificationPublisher mailNotificationPublisher) {
    this.mailNotificationPublisher = mailNotificationPublisher;
  }

  public void notifyCreated(RentalRequest request) {
    String email = request.getCustomer().getEmail();
    if (email == null || email.isBlank()) {
      return;
    }
    Map<String, Object> payload = commonPayload(request);
    payload.put("eventType", "created");
    mailNotificationPublisher.publish(
        MailSendRequestedEvent.of(
            email.trim(),
            "Kiralama talebiniz alındı",
            "rent.request.created",
            payload));
  }

  public void notifyStatusChanged(RentalRequest request) {
    String email = request.getCustomer().getEmail();
    if (email == null || email.isBlank()) {
      return;
    }
    Map<String, Object> payload = commonPayload(request);
    payload.put("eventType", "statusChanged");
    payload.put("statusMessage", request.getStatusMessage());
    mailNotificationPublisher.publish(
        MailSendRequestedEvent.of(
            email.trim(),
            "Kiralama talep durumunuz güncellendi",
            "rent.request.status.updated",
            payload));
  }

  private static Map<String, Object> commonPayload(RentalRequest request) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("referenceNo", request.getReferenceNo());
    payload.put("status", request.getStatus().name());
    payload.put("startDate", request.getStartDate().toString());
    payload.put("endDate", request.getEndDate().toString());
    payload.put("fullName", request.getCustomer().getFullName());
    payload.put("phone", request.getCustomer().getPhone());
    payload.put("outsideCountryTravel", request.isOutsideCountryTravel());
    payload.put("greenInsuranceFee", request.getGreenInsuranceFee());
    return payload;
  }
}
