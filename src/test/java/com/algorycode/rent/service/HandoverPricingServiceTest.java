package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.HandoverPricingQuoteDto;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.domain.location.HandoverLocationKind;
import com.algorycode.rent.repository.HandoverLocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandoverPricingServiceTest {

  @Mock private HandoverLocationRepository handoverLocationRepository;

  @InjectMocks private HandoverPricingService handoverPricingService;

  @Test
  void quote_sameId_returnsZero() {
    long id = 1L;
    HandoverPricingQuoteDto q = handoverPricingService.quote(id, id);
    assertThat(q.totalEur()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(q.applied()).isFalse();
  }

  @Test
  void quote_alToXK_route60_plus_legs() {
    long pId = 10L;
    long rId = 11L;
    when(handoverLocationRepository.findById(pId)).thenReturn(Optional.of(loc(pId, "AL", new BigDecimal("25"))));
    when(handoverLocationRepository.findById(rId)).thenReturn(Optional.of(loc(rId, "XK", new BigDecimal("0"))));
    HandoverPricingQuoteDto q = handoverPricingService.quote(pId, rId);
    assertThat(q.pickupLegEur()).isEqualByComparingTo("25.00");
    assertThat(q.returnLegEur()).isEqualByComparingTo("0.00");
    assertThat(q.routeEur()).isEqualByComparingTo("60.00");
    assertThat(q.totalEur()).isEqualByComparingTo("85.00");
    assertThat(q.applied()).isTrue();
  }

  private static HandoverLocation loc(long id, String countryCode, BigDecimal surcharge) {
    HandoverLocation h = new HandoverLocation();
    h.setId(id);
    h.setKind(HandoverLocationKind.PICKUP);
    h.setName("Test");
    h.setLineOrder(0);
    h.setCountryCode(countryCode);
    h.setSurchargeEur(surcharge);
    return h;
  }
}
