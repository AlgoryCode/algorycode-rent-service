package com.algorycode.rent.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

  public static final String MAIL_QUEUE = "rent.mail.queue";

  @Bean
  TopicExchange rentMailExchange(AppMailProperties props) {
    return ExchangeBuilder.topicExchange(props.rabbit().exchange()).durable(true).build();
  }

  @Bean
  Queue rentMailQueue() {
    return QueueBuilder.durable(MAIL_QUEUE).build();
  }

  @Bean
  Binding rentMailBinding(Queue rentMailQueue, TopicExchange rentMailExchange, AppMailProperties props) {
    return BindingBuilder.bind(rentMailQueue)
        .to(rentMailExchange)
        .with(props.rabbit().routingKey());
  }
}
