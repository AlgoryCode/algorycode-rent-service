package com.algorycode.rent.messaging;

import com.algorycode.rent.config.AppMailProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMailNotificationPublisher implements MailNotificationPublisher {

  private final RabbitTemplate rabbitTemplate;
  private final AppMailProperties props;

  public RabbitMailNotificationPublisher(
      RabbitTemplate rabbitTemplate, AppMailProperties props) {
    this.rabbitTemplate = rabbitTemplate;
    this.props = props;
  }

  @Override
  public void publish(MailSendRequestedEvent event) {
    rabbitTemplate.convertAndSend(
        props.rabbit().exchange(), props.rabbit().routingKey(), event);
  }
}
