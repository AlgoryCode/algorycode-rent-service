package com.algorycode.rent.service.support;

import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalOption;
import com.algorycode.rent.domain.vehicle.Vehicle;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RentalRevenueEurTest {

  @Test
  void inclusiveRentalDays_singleDay() {
    LocalDate d = LocalDate.of(2026, 4, 1);
    assertThat(RentalRevenueEur.inclusiveRentalDays(d, d)).isEqualTo(1);
  }

  @Test
  void inclusiveRentalDays_span() {
    assertThat(
            RentalRevenueEur.inclusiveRentalDays(
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 3)))
        .isEqualTo(3);
  }

  @Test
  void inclusiveRentalDays_returnsZeroWhenNullOrInverted() {
    assertThat(RentalRevenueEur.inclusiveRentalDays(null, LocalDate.now())).isZero();
    assertThat(RentalRevenueEur.inclusiveRentalDays(LocalDate.now(), null)).isZero();
    assertThat(
            RentalRevenueEur.inclusiveRentalDays(
                LocalDate.of(2026, 4, 5), LocalDate.of(2026, 4, 4)))
        .isZero();
  }

  @Test
  void baseRentalEur_multipliesDailyByInclusiveDays() {
    Rental r = new Rental();
    Vehicle v = new Vehicle();
    v.setRentalDailyPrice(new BigDecimal("50.00"));
    r.setVehicle(v);
    r.setStartDate(LocalDate.of(2026, 1, 1));
    r.setEndDate(LocalDate.of(2026, 1, 3));
    assertThat(RentalRevenueEur.baseRentalEur(r)).isEqualByComparingTo("150.00");
  }

  @Test
  void baseRentalEur_treatsMissingVehicleOrPriceAsZero() {
    Rental noVehicle = new Rental();
    noVehicle.setStartDate(LocalDate.of(2026, 1, 1));
    noVehicle.setEndDate(LocalDate.of(2026, 1, 5));
    assertThat(RentalRevenueEur.baseRentalEur(noVehicle)).isEqualByComparingTo(BigDecimal.ZERO);

    Rental nullPrice = new Rental();
    Vehicle v = new Vehicle();
    v.setRentalDailyPrice(null);
    nullPrice.setVehicle(v);
    nullPrice.setStartDate(LocalDate.of(2026, 1, 1));
    nullPrice.setEndDate(LocalDate.of(2026, 1, 2));
    assertThat(RentalRevenueEur.baseRentalEur(nullPrice)).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void optionsTotal_sumsNonNullPrices() {
    Rental r = new Rental();
    List<RentalOption> opts = new ArrayList<>();
    RentalOption a = new RentalOption();
    a.setPrice(new BigDecimal("10.25"));
    RentalOption b = new RentalOption();
    b.setPrice(null);
    RentalOption c = new RentalOption();
    c.setPrice(new BigDecimal("2.75"));
    opts.add(a);
    opts.add(b);
    opts.add(c);
    r.setOptions(opts);
    assertThat(RentalRevenueEur.optionsTotal(r)).isEqualByComparingTo("13.00");
  }

  @Test
  void optionsTotal_emptyOrNullList() {
    Rental r = new Rental();
    r.setOptions(null);
    assertThat(RentalRevenueEur.optionsTotal(r)).isEqualByComparingTo(BigDecimal.ZERO);
    r.setOptions(new ArrayList<>());
    assertThat(RentalRevenueEur.optionsTotal(r)).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void totalRentalRevenueEur_scalesToTwoDecimalsHalfUp() {
    Rental r = new Rental();
    Vehicle v = new Vehicle();
    v.setRentalDailyPrice(new BigDecimal("33.33"));
    r.setVehicle(v);
    r.setStartDate(LocalDate.of(2026, 6, 1));
    r.setEndDate(LocalDate.of(2026, 6, 2));
    RentalOption o = new RentalOption();
    o.setPrice(new BigDecimal("0.015"));
    r.setOptions(List.of(o));
    assertThat(RentalRevenueEur.totalRentalRevenueEur(r)).isEqualByComparingTo("66.68");
  }
}
