package com.algorycode.rent.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.algorycode.rent.config.AppMailProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class RabbitMailNotificationPublisherTest {

  @Mock private RabbitTemplate rabbitTemplate;

  private RabbitMailNotificationPublisher publisher;

  @BeforeEach
  void setUp() {
    var props =
        new AppMailProperties(new AppMailProperties.Rabbit("ex.test", "rk.test", "dlq.test"));
    publisher = new RabbitMailNotificationPublisher(rabbitTemplate, props, new ObjectMapper());
  }

  @Test
  void publish_sendsJsonUtf8ToConfiguredExchangeAndRoutingKey() {
    var msg = QueuedMailMessage.plain("user@test.com", "Konu", "Gövde metni");

    publisher.publish(msg);

    var captor = ArgumentCaptor.forClass(Message.class);
    verify(rabbitTemplate).send(eq("ex.test"), eq("rk.test"), captor.capture());
    Message sent = captor.getValue();
    assertThat(sent.getMessageProperties().getContentType()).contains("json");
    String json = new String(sent.getBody(), StandardCharsets.UTF_8);
    assertThat(json).contains("user@test.com").contains("Konu").contains("Gövde metni");
  }
}
