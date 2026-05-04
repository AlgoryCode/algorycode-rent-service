package com.algorycode.rent.logging;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class AuditLogWhitelistTest {

  private ListAppender<ILoggingEvent> listAppender;
  private Logger auditLogger;

  @BeforeEach
  void setUp() {
    auditLogger = (Logger) LoggerFactory.getLogger("AUDIT");
    listAppender = new ListAppender<>();
    listAppender.start();
    auditLogger.addAppender(listAppender);
  }

  @AfterEach
  void tearDown() {
    auditLogger.detachAppender(listAppender);
    listAppender.stop();
  }

  @Test
  void infoEvent_omitsKeysNotOnWhitelist() {
    AuditLog auditLog = new AuditLog();
    String rentalId = "42";
    auditLog.infoEvent(
        "probe_event",
        Map.of(
            "rentalId", rentalId,
            "customerName", "SECRET_NAME",
            "phone", "5550000"));

    assertThat(listAppender.list).hasSize(1);
    String msg = listAppender.list.get(0).getFormattedMessage();
    assertThat(msg).contains("rentalId=" + rentalId);
    assertThat(msg).doesNotContain("SECRET_NAME");
    assertThat(msg).doesNotContain("customerName");
    assertThat(msg).doesNotContain("5550000");
    assertThat(msg).doesNotContain("phone=");
  }
}
