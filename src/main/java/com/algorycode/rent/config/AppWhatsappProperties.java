package com.algorycode.rent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.whatsapp")
public record AppWhatsappProperties(
    boolean enabled,
    String adminPhoneE164,
    String webhookUrl,
    String webhookAuthBearer,
    String callmebotApiKey) {

  public AppWhatsappProperties {
    adminPhoneE164 = adminPhoneE164 == null ? "" : adminPhoneE164.trim();
    webhookUrl = webhookUrl == null ? "" : webhookUrl.trim();
    webhookAuthBearer = webhookAuthBearer == null ? "" : webhookAuthBearer.trim();
    callmebotApiKey = callmebotApiKey == null ? "" : callmebotApiKey.trim();
  }
}
