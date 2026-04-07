package com.algorycode.rent.service;

import com.algorycode.rent.domain.user.PanelUser;
import com.algorycode.rent.domain.user.PanelUserRole;
import com.algorycode.rent.repository.PanelUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PanelUserServiceTest {

  @Mock private PanelUserRepository panelUserRepository;

  @InjectMocks private PanelUserService panelUserService;

  @Test
  void listAll_returnsDtos() {
    var u = new PanelUser();
    u.setId(UUID.randomUUID());
    u.setFullName("Admin");
    u.setEmail("a@x.com");
    u.setRole(PanelUserRole.admin);
    u.setLastActiveAt(Instant.parse("2026-01-01T00:00:00Z"));
    u.setActive(true);
    u.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
    u.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
    when(panelUserRepository.findAll()).thenReturn(List.of(u));

    var rows = panelUserService.listAll();

    assertThat(rows).hasSize(1);
    assertThat(rows.getFirst().email()).isEqualTo("a@x.com");
    assertThat(rows.getFirst().role()).isEqualTo(PanelUserRole.admin);
  }
}
