package com.algorycode.rent.messaging;

import com.algorycode.rent.config.AppMailProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMailNotificationPublisher implements MailNotificationPublisher {

  private final RabbitTemplate rabbitTemplate;
  private final AppMailProperties props;
  private final ObjectMapper objectMapper;

  public RabbitMailNotificationPublisher(
      RabbitTemplate rabbitTemplate, AppMailProperties props, ObjectMapper objectMapper) {
    this.rabbitTemplate = rabbitTemplate;
    this.props = props;
    this.objectMapper = objectMapper;
  }

  @Override
  public void publish(QueuedMailMessage message) {
    try {
      byte[] body = objectMapper.writeValueAsBytes(message);
      MessageProperties mp = new MessageProperties();
      mp.setContentType(MessageProperties.CONTENT_TYPE_JSON);
      mp.setContentEncoding("UTF-8");
      rabbitTemplate.send(
          props.rabbit().exchange(), props.rabbit().routingKey(), new Message(body, mp));
    } catch (Exception e) {
      throw new IllegalStateException("Mail kuyruğuna JSON yazılamadı: " + e.getMessage(), e);
    }
  }
}
