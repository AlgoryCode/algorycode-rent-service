package com.algorycode.rent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.algorycode.rent.exception.ResourceNotFoundException;
import com.algorycode.rent.entity.PanelUser;
import com.algorycode.rent.entity.PanelUserRole;
import com.algorycode.rent.repository.PanelUserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PanelUserServiceTest {

  @Mock private PanelUserRepository panelUserRepository;

  @InjectMocks private PanelUserService panelUserService;

  @Test
  void listAll_returnsDtos() {
    var u = new PanelUser();
    u.setId(1L);
    u.setFullName("Admin");
    u.setEmail("a@x.com");
    u.setRole(PanelUserRole.admin);
    u.setLastActiveAt(Instant.parse("2026-01-01T00:00:00Z"));
    u.setActive(true);
    u.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
    u.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
    when(panelUserRepository.findAllByDeletedFalse()).thenReturn(List.of(u));

    var rows = panelUserService.listAll();

    assertThat(rows).hasSize(1);
    assertThat(rows.getFirst().email()).isEqualTo("a@x.com");
    assertThat(rows.getFirst().role()).isEqualTo(PanelUserRole.admin);
  }

  @Test
  void deleteById_softDeletesWhenActive() {
    var id = 1L;
    var u = new PanelUser();
    u.setId(id);
    u.setDeleted(false);
    when(panelUserRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(u));

    panelUserService.deleteById(id);

    assertThat(u.isDeleted()).isTrue();
    verify(panelUserRepository).save(u);
  }

  @Test
  void deleteById_throwsWhenMissing() {
    var id = 1L;
    when(panelUserRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> panelUserService.deleteById(id))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("bulunamadı");
  }
}
