package com.algorycode.rent.service;

import com.algorycode.rent.dto.report.RentalDashboardReportDto;
import com.algorycode.rent.dto.report.RentalDashboardReportDto.RentalReportSummary;
import com.algorycode.rent.dto.report.RentalDashboardReportDto.TimelineBucket;
import com.algorycode.rent.dto.report.RentalDashboardReportDto.VehicleRentalStatRow;
import com.algorycode.rent.exception.BadRequestException;
import com.algorycode.rent.entity.Rental;
import com.algorycode.rent.entity.RentalStatus;
import com.algorycode.rent.entity.Vehicle;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.service.support.RentalRevenueEur;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalReportService {

  private static final int DAILY_TIMELINE_MAX_DAYS = 93;
  private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy");

  private final RentalRepository rentalRepository;

  @Transactional(readOnly = true)
  public RentalDashboardReportDto rentalDashboard(LocalDate from, LocalDate to, Long vehicleId) {
    LocalDate toEff = to != null ? to : LocalDate.now();
    LocalDate fromEff = from != null ? from : toEff.minusMonths(1);
    if (toEff.isBefore(fromEff)) {
      throw new BadRequestException("Bitiş tarihi başlangıçtan önce olamaz.");
    }

    List<Rental> rentals =
        rentalRepository.findForRevenueReport(fromEff, toEff, vehicleId).stream()
            .filter(r -> r.getStatus() != RentalStatus.cancelled)
            .toList();

    long daysInRange = ChronoUnit.DAYS.between(fromEff, toEff) + 1;
    boolean useDaily = daysInRange <= DAILY_TIMELINE_MAX_DAYS;
    String granularity = useDaily ? "day" : "month";

    BigDecimal totalRev = BigDecimal.ZERO;
    BigDecimal totalBase = BigDecimal.ZERO;
    BigDecimal totalOpts = BigDecimal.ZERO;
    BigDecimal totalComm = BigDecimal.ZERO;
    long rentalDaySum = 0;
    int activePending = 0;
    int completed = 0;

    Map<Long, VehicleAgg> byV = new HashMap<>();
    Map<String, TimelineAgg> timeline = new HashMap<>();

    for (Rental r : rentals) {
      BigDecimal base = RentalRevenueEur.baseRentalEur(r);
      BigDecimal opts = RentalRevenueEur.optionsTotal(r);
      BigDecimal rev = RentalRevenueEur.totalRentalRevenueEur(r);
      base = base.setScale(2, RoundingMode.HALF_UP);
      opts = opts.setScale(2, RoundingMode.HALF_UP);
      BigDecimal comm =
          r.getCommissionAmount() != null
              ? r.getCommissionAmount().setScale(2, RoundingMode.HALF_UP)
              : BigDecimal.ZERO;
      long incDays = RentalRevenueEur.inclusiveRentalDays(r.getStartDate(), r.getEndDate());

      totalRev = totalRev.add(rev);
      totalBase = totalBase.add(base);
      totalOpts = totalOpts.add(opts);
      totalComm = totalComm.add(comm);
      rentalDaySum += incDays;

      if (r.getStatus() == RentalStatus.completed) {
        completed++;
      } else if (r.getStatus() == RentalStatus.active || r.getStatus() == RentalStatus.pending) {
        activePending++;
      }

      Vehicle v = r.getVehicle();
      Long vid = v != null ? v.getId() : null;
      if (vid != null) {
        VehicleAgg a = byV.computeIfAbsent(vid, k -> new VehicleAgg(v));
        a.rentalCount++;
        a.rentalDayBooked += incDays;
        a.revenue = a.revenue.add(rev);
        a.base = a.base.add(base);
        a.options = a.options.add(opts);
      }

      String bucketKey = bucketKeyFor(r.getStartDate(), useDaily);
      if (bucketKey != null) {
        TimelineAgg t = timeline.computeIfAbsent(bucketKey, k -> new TimelineAgg());
        t.rentalStarts++;
        t.revenue = t.revenue.add(rev);
      }
    }

    List<VehicleRentalStatRow> vehicleRows =
        byV.values().stream()
            .map(VehicleAgg::toRow)
            .sorted(Comparator.comparing(VehicleRentalStatRow::revenueEur).reversed())
            .toList();

    List<TimelineBucket> timelineRows = buildTimelineBuckets(fromEff, toEff, useDaily, timeline);

    RentalReportSummary summary =
        new RentalReportSummary(
            rentals.size(),
            rentalDaySum,
            totalRev.setScale(2, RoundingMode.HALF_UP),
            totalBase.setScale(2, RoundingMode.HALF_UP),
            totalOpts.setScale(2, RoundingMode.HALF_UP),
            totalComm.setScale(2, RoundingMode.HALF_UP),
            activePending,
            completed);

    return new RentalDashboardReportDto(
        fromEff, toEff, granularity, summary, vehicleRows, timelineRows);
  }

  private static String bucketKeyFor(LocalDate rentalStart, boolean useDaily) {
    if (useDaily) {
      return rentalStart.toString();
    }
    YearMonth ym = YearMonth.from(rentalStart);
    return ym.toString();
  }

  private static List<TimelineBucket> buildTimelineBuckets(
      LocalDate from, LocalDate to, boolean useDaily, Map<String, TimelineAgg> data) {
    List<TimelineBucket> out = new ArrayList<>();
    if (useDaily) {
      for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
        String key = d.toString();
        TimelineAgg a = data.getOrDefault(key, new TimelineAgg());
        out.add(
            new TimelineBucket(
                key,
                d.format(DateTimeFormatter.ofPattern("dd/MM")),
                a.rentalStarts,
                a.revenue.setScale(2, RoundingMode.HALF_UP)));
      }
    } else {
      YearMonth startYm = YearMonth.from(from);
      YearMonth endYm = YearMonth.from(to);
      for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
        String key = ym.toString();
        TimelineAgg a = data.getOrDefault(key, new TimelineAgg());
        out.add(
            new TimelineBucket(
                key,
                ym.format(MONTH_LABEL),
                a.rentalStarts,
                a.revenue.setScale(2, RoundingMode.HALF_UP)));
      }
    }
    return out;
  }

  private static final class VehicleAgg {
    final Vehicle vehicle;
    int rentalCount;
    long rentalDayBooked;
    BigDecimal revenue = BigDecimal.ZERO;
    BigDecimal base = BigDecimal.ZERO;
    BigDecimal options = BigDecimal.ZERO;

    VehicleAgg(Vehicle vehicle) {
      this.vehicle = vehicle;
    }

    VehicleRentalStatRow toRow() {
      return new VehicleRentalStatRow(
          vehicle.getId(),
          vehicle.getPlate() != null ? vehicle.getPlate() : "",
          vehicle.getBrand() != null ? vehicle.getBrand() : "",
          vehicle.getModel() != null ? vehicle.getModel() : "",
          rentalCount,
          rentalDayBooked,
          revenue.setScale(2, RoundingMode.HALF_UP),
          base.setScale(2, RoundingMode.HALF_UP),
          options.setScale(2, RoundingMode.HALF_UP));
    }
  }

  private static final class TimelineAgg {
    int rentalStarts;
    BigDecimal revenue = BigDecimal.ZERO;
  }
}
