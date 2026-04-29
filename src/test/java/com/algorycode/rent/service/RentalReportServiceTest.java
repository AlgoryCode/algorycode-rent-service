package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.report.RentalDashboardReportDto;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalOption;
import com.algorycode.rent.domain.rental.RentalStatus;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleStatus;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.service.support.VehicleTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalReportServiceTest {

  @Mock private RentalRepository rentalRepository;

  @InjectMocks private RentalReportService rentalReportService;

  @Test
  void rentalDashboard_throwsWhenEndBeforeStart() {
    LocalDate from = LocalDate.of(2026, 4, 10);
    LocalDate to = LocalDate.of(2026, 4, 1);
    assertThatThrownBy(() -> rentalReportService.rentalDashboard(from, to, null))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Bitiş tarihi başlangıçtan önce olamaz");
  }

  @Test
  void rentalDashboard_usesDayGranularityForShortRange() {
    LocalDate from = LocalDate.of(2026, 1, 1);
    LocalDate to = LocalDate.of(2026, 1, 10);
    when(rentalRepository.findForRevenueReport(from, to, null)).thenReturn(List.of());

    RentalDashboardReportDto dto = rentalReportService.rentalDashboard(from, to, null);

    assertThat(dto.timelineGranularity()).isEqualTo("day");
    assertThat(dto.timeline()).hasSize(10);
    assertThat(dto.summary().rentalCount()).isZero();
    assertThat(dto.summary().totalRevenueEur()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void rentalDashboard_usesMonthGranularityWhenRangeExceedsDailyCap() {
    LocalDate from = LocalDate.of(2026, 1, 1);
    LocalDate to = from.plusDays(93);
    when(rentalRepository.findForRevenueReport(from, to, null)).thenReturn(List.of());

    RentalDashboardReportDto dto = rentalReportService.rentalDashboard(from, to, null);

    assertThat(dto.timelineGranularity()).isEqualTo("month");
    assertThat(dto.timeline()).isNotEmpty();
  }

  @Test
  void rentalDashboard_aggregatesSingleRentalAndVehicleRow() {
    LocalDate from = LocalDate.of(2026, 1, 1);
    LocalDate to = LocalDate.of(2026, 1, 31);
    Long vehicleId = 1L;

    Vehicle v = new Vehicle();
    v.setId(vehicleId);
    v.setPlate("34 X 1");
    VehicleTestFixtures.attachBrandModelStatus(v, "Test", "Car", VehicleStatus.available);
    v.setRentalDailyPrice(new BigDecimal("100.00"));

    RentalOption opt = new RentalOption();
    opt.setPrice(new BigDecimal("25.50"));

    Rental r = new Rental();
    r.setVehicle(v);
    r.setStartDate(LocalDate.of(2026, 1, 5));
    r.setEndDate(LocalDate.of(2026, 1, 7));
    r.setStatus(RentalStatus.active);
    r.setCommissionAmount(new BigDecimal("12.34"));
    r.setOptions(List.of(opt));

    when(rentalRepository.findForRevenueReport(from, to, null)).thenReturn(List.of(r));

    RentalDashboardReportDto dto = rentalReportService.rentalDashboard(from, to, null);

    assertThat(dto.summary().rentalCount()).isEqualTo(1);
    assertThat(dto.summary().rentalDayBooked()).isEqualTo(3);
    assertThat(dto.summary().activeOrPendingCount()).isEqualTo(1);
    assertThat(dto.summary().completedCount()).isZero();
    assertThat(dto.summary().totalBaseRentalEur()).isEqualByComparingTo("300.00");
    assertThat(dto.summary().totalOptionsEur()).isEqualByComparingTo("25.50");
    assertThat(dto.summary().totalRevenueEur()).isEqualByComparingTo("325.50");
    assertThat(dto.summary().totalCommissionEur()).isEqualByComparingTo("12.34");

    assertThat(dto.byVehicle()).hasSize(1);
    assertThat(dto.byVehicle().getFirst().vehicleId()).isEqualTo(vehicleId);
    assertThat(dto.byVehicle().getFirst().revenueEur()).isEqualByComparingTo("325.50");
  }

  @Test
  void rentalDashboard_excludesCancelledFromAggregates() {
    LocalDate from = LocalDate.of(2026, 2, 1);
    LocalDate to = LocalDate.of(2026, 2, 28);

    Rental cancelled = new Rental();
    cancelled.setStatus(RentalStatus.cancelled);
    cancelled.setVehicle(new Vehicle());
    cancelled.getVehicle().setId(1L);
    cancelled.getVehicle().setRentalDailyPrice(BigDecimal.TEN);
    cancelled.setStartDate(from);
    cancelled.setEndDate(from);

    when(rentalRepository.findForRevenueReport(eq(from), eq(to), any()))
        .thenReturn(List.of(cancelled));

    RentalDashboardReportDto dto = rentalReportService.rentalDashboard(from, to, null);

    assertThat(dto.summary().rentalCount()).isZero();
    assertThat(dto.byVehicle()).isEmpty();
  }
}
