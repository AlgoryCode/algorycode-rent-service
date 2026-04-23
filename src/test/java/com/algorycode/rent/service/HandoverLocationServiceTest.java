package com.algorycode.rent.service;

import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.domain.location.HandoverLocationKind;
import com.algorycode.rent.repository.HandoverLocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandoverLocationServiceTest {

  @Mock private HandoverLocationRepository handoverLocationRepository;

  @InjectMocks private HandoverLocationService handoverLocationService;

  @Test
  void requireActive_returnsNullWhenIdNull() {
    assertThat(handoverLocationService.requireActive(null)).isNull();
  }

  @Test
  void requireActive_returnsEntityWhenActive() {
    Long id = 1L;
    HandoverLocation loc = new HandoverLocation();
    loc.setId(id);
    loc.setKind(HandoverLocationKind.RETURN);
    loc.setName("Teslim A");
    loc.setActive(true);
    loc.setLineOrder(1);
    when(handoverLocationRepository.findById(id)).thenReturn(Optional.of(loc));

    assertThat(handoverLocationService.requireActive(id)).isSameAs(loc);
  }

  @Test
  void requireActive_throwsWhenMissing() {
    Long id = 1L;
    when(handoverLocationRepository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> handoverLocationService.requireActive(id))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void requireActive_throwsWhenInactive() {
    Long id = 1L;
    HandoverLocation loc = new HandoverLocation();
    loc.setId(id);
    loc.setKind(HandoverLocationKind.PICKUP);
    loc.setName("X");
    loc.setActive(false);
    loc.setLineOrder(0);
    when(handoverLocationRepository.findById(id)).thenReturn(Optional.of(loc));
    assertThatThrownBy(() -> handoverLocationService.requireActive(id))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("kullanılamaz");
  }
}
