package com.algorycode.rent.service.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.algorycode.rent.entity.Customer;
import com.algorycode.rent.entity.Rental;
import com.algorycode.rent.entity.RentalStatus;
import com.algorycode.rent.entity.RentalRequest;
import com.algorycode.rent.entity.RentalRequestStatus;
import com.algorycode.rent.entity.Vehicle;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VehicleAvailabilitySlotAnalyzerTest {

  private VehicleAvailabilitySlotAnalyzer analyzer;

  @BeforeEach
  void setUp() {
    analyzer = new VehicleAvailabilitySlotAnalyzer();
  }

  @Test
  void mergeSorted_whenAdjacentIntervals_thenMergesIntoOne() {
    var a = new InclusiveLocalDateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2));
    var b = new InclusiveLocalDateRange(LocalDate.of(2026, 6, 3), LocalDate.of(2026, 6, 5));
    List<InclusiveLocalDateRange> merged = analyzer.mergeSorted(List.of(b, a));
    assertThat(merged)
        .containsExactly(
            new InclusiveLocalDateRange(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5)));
  }

  @Test
  void freeSlotsInWindow_whenBlockInMiddle_thenTwoGaps() {
    var merged =
        List.of(new InclusiveLocalDateRange(LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 12)));
    List<InclusiveLocalDateRange> free =
        analyzer.freeSlotsInWindow(LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 15), merged);
    assertThat(free)
        .containsExactly(
            new InclusiveLocalDateRange(LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 9)),
            new InclusiveLocalDateRange(LocalDate.of(2026, 6, 13), LocalDate.of(2026, 6, 15)));
  }

  @Test
  void isAvailableForInclusiveTrip_whenTurnoverDayOccupied_thenFalse() {
    long vid = 1L;
    Rental r = rental(vid, LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 16));
    boolean ok =
        analyzer.isAvailableForInclusiveTrip(
            vid, LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 14), List.of(r), List.of());
    assertThat(ok).isFalse();
  }

  @Test
  void isAvailableForInclusiveTrip_whenTripInGapAndTurnoverFree_thenTrue() {
    long vid = 1L;
    Rental r = rental(vid, LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 22));
    boolean ok =
        analyzer.isAvailableForInclusiveTrip(
            vid, LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 15), List.of(r), List.of());
    assertThat(ok).isTrue();
  }

  @Test
  void inclusiveTripFitsInAFreeSlot_whenTripSpansGapBetweenMergedBlocks_thenFalse() {
    long vid = 1L;
    List<InclusiveLocalDateRange> merged =
        analyzer.mergeSorted(
            analyzer.collectRawIntervals(
                vid,
                List.of(
                    rental(vid, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2)),
                    rental(vid, LocalDate.of(2026, 6, 4), LocalDate.of(2026, 6, 5))),
                List.of()));
    LocalDate w0 = LocalDate.of(2026, 6, 1);
    LocalDate w1 = LocalDate.of(2026, 6, 5);
    assertThat(analyzer.inclusiveTripFitsInAFreeSlot(w0, w1, w0, w1, merged)).isFalse();
  }

  @Test
  void isAvailableForInclusiveTrip_whenRentalRequestOverlapsTrip_thenFalse() {
    long vid = 2L;
    RentalRequest rr = new RentalRequest();
    rr.setStatus(RentalRequestStatus.pending);
    rr.setStartDate(LocalDate.of(2026, 8, 10));
    rr.setEndDate(LocalDate.of(2026, 8, 11));
    Vehicle rv = new Vehicle();
    rv.setId(vid);
    rv.setPlate("34 AVA 1");
    rr.setVehicle(rv);
    boolean ok =
        analyzer.isAvailableForInclusiveTrip(
            vid, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 11), List.of(), List.of(rr));
    assertThat(ok).isFalse();
  }

  private static Rental rental(long vehicleId, LocalDate start, LocalDate end) {
    Rental r = new Rental();
    r.setStartDate(start);
    r.setEndDate(end);
    RentalTestFixtures.attachRentalStatus(r, RentalStatus.ACTIVE);
    Vehicle v = new Vehicle();
    v.setId(vehicleId);
    v.setPlate("34 AVA 2");
    r.setVehicle(v);
    Customer c =
        Customer.builder().fullName("Test").nationalId("").passportNo("").phone("1").build();
    c.setId(1L);
    r.setCustomerId(1L);
    r.setCustomer(c);
    return r;
  }
}
