package com.algorycode.rent.service.support;

import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.domain.rental.RentalCommissionFlow;
import java.math.BigDecimal;

/** Kiralama komisyon tutarı ve {@code pay} akışında firma adı kuralları. */
public final class RentalCommissionValidator {

  public static final String MSG_NEGATIVE = "Komisyon tutarı negatif olamaz.";
  public static final String MSG_PAY_COMPANY = "Komisyon ödemesinde firma adı zorunludur.";

  private RentalCommissionValidator() {}

  /**
   * @param commissionCompany ham veya trimlenmiş; {@code pay} + pozitif tutarda boş olamaz
   */
  public static void validate(
      BigDecimal amount, RentalCommissionFlow flow, String commissionCompany) {
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException(MSG_NEGATIVE);
    }
    if (flow == RentalCommissionFlow.pay
        && amount.compareTo(BigDecimal.ZERO) > 0
        && (commissionCompany == null || commissionCompany.isBlank())) {
      throw new BadRequestException(MSG_PAY_COMPANY);
    }
  }
}
