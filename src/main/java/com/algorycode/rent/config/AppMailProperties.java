package com.algorycode.rent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record AppMailProperties(Rabbit rabbit) {

  public record Rabbit(String exchange, String routingKey, String queueDlq) {}
}
