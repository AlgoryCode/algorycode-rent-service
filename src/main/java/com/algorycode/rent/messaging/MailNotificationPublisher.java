package com.algorycode.rent.messaging;

/**
 * SOLID: üst seviye servisler mail detayından kopuk — sadece bu arayüze bağımlı.
 */
@FunctionalInterface
public interface MailNotificationPublisher {

  void publish(MailSendRequestedEvent event);
}
