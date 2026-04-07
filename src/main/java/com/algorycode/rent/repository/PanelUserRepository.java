package com.algorycode.rent.repository;

import com.algorycode.rent.domain.user.PanelUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PanelUserRepository extends JpaRepository<PanelUser, UUID> {

  Optional<PanelUser> findByEmailIgnoreCase(String email);
}
