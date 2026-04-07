package com.algorycode.rent.messaging;

import java.util.Map;
import java.util.UUID;

/**
 * Mail servisi tüketir — RabbitMQ üzerinden asenkron gönderim talebi.
 */
public record MailSendRequestedEvent(
    UUID eventId,
    String to,
    String subject,
    String templateCode,
    Map<String, Object> payload) {

  public static MailSendRequestedEvent of(
      String to, String subject, String templateCode, Map<String, Object> payload) {
    return new MailSendRequestedEvent(UUID.randomUUID(), to, subject, templateCode, Map.copyOf(payload));
  }
}
