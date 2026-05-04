package com.algorycode.rent.messaging;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * JSON alan adları {@code mail-service} içindeki {@code MailMessage} ile uyumludur; Rabbit
 * üzerinden düz JSON gönderilir (Java serileştirme / uyumsuz type-id kullanılmaz).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record QueuedMailMessage(
    String to, String subject, String body, String htmlBody, String templateCode) {

  public QueuedMailMessage {
    htmlBody = htmlBody == null || htmlBody.isBlank() ? null : htmlBody;
    templateCode = templateCode == null || templateCode.isBlank() ? null : templateCode.trim();
  }

  public static QueuedMailMessage plain(String to, String subject, String body) {
    return new QueuedMailMessage(to, subject, body, null, null);
  }

  public static QueuedMailMessage plain(
      String to, String subject, String body, String templateCode) {
    return new QueuedMailMessage(to, subject, body, null, templateCode);
  }

  public static QueuedMailMessage multipart(
      String to, String subject, String plainBody, String htmlBody) {
    return new QueuedMailMessage(to, subject, plainBody, htmlBody, null);
  }

  public static QueuedMailMessage multipart(
      String to, String subject, String plainBody, String htmlBody, String templateCode) {
    return new QueuedMailMessage(to, subject, plainBody, htmlBody, templateCode);
  }
}
