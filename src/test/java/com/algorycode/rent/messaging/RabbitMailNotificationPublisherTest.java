package com.algorycode.rent.messaging;

import com.algorycode.rent.config.AppMailProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMailNotificationPublisherTest {

  @Mock private RabbitTemplate rabbitTemplate;

  private RabbitMailNotificationPublisher publisher;

  @BeforeEach
  void setUp() {
    var props =
        new AppMailProperties(new AppMailProperties.Rabbit("ex.test", "rk.test", "dlq.test"));
    publisher = new RabbitMailNotificationPublisher(rabbitTemplate, props);
  }

  @Test
  void publish_sendsToConfiguredExchangeAndRoutingKey() {
    var event =
        MailSendRequestedEvent.of(
            "user@test.com", "Konu", "RENTAL_CONFIRM", Map.of("rentalId", "abc"));

    publisher.publish(event);

    var captor = ArgumentCaptor.forClass(MailSendRequestedEvent.class);
    verify(rabbitTemplate).convertAndSend(eq("ex.test"), eq("rk.test"), captor.capture());
    assertThat(captor.getValue().to()).isEqualTo("user@test.com");
    assertThat(captor.getValue().templateCode()).isEqualTo("RENTAL_CONFIRM");
  }
}
