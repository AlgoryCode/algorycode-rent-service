package com.algorycode.rent.config;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Yalnızca topic exchange tanımlanır; kuyruk ve binding {@code mail-service} tarafında ({@code
 * algorycode.mail.work-queue} + {@code mail.#}) oluşturulur. Rent yayıncıdır.
 */
@Configuration
public class RabbitMqConfig {

  @Bean
  TopicExchange mailTopicExchange(AppMailProperties props) {
    return ExchangeBuilder.topicExchange(props.rabbit().exchange()).durable(true).build();
  }
}
