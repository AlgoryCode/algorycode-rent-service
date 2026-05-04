package com.algorycode.rent.service.support;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.algorycode.rent.api.error.BadRequestException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DateRangeValidatorTest {

  @Test
  void requireEndNotBeforeStart_throwsWhenEndBeforeStart() {
    assertThatThrownBy(
            () ->
                DateRangeValidator.requireEndNotBeforeStart(
                    LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 9)))
        .isInstanceOf(BadRequestException.class)
        .hasMessage(DateRangeValidator.MSG_END_BEFORE_START);
  }

  @Test
  void requireEndNotBeforeStart_okWhenSameDay() {
    assertThatCode(
            () ->
                DateRangeValidator.requireEndNotBeforeStart(
                    LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 10)))
        .doesNotThrowAnyException();
  }

  @Test
  void requireEndNotBeforeStartIfBothPresent_skipsWhenEitherNull() {
    assertThatCode(
            () -> DateRangeValidator.requireEndNotBeforeStartIfBothPresent(null, LocalDate.now()))
        .doesNotThrowAnyException();
    assertThatCode(
            () -> DateRangeValidator.requireEndNotBeforeStartIfBothPresent(LocalDate.now(), null))
        .doesNotThrowAnyException();
  }
}
