package com.algorycode.rent.repository;

import com.algorycode.rent.domain.user.PanelUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PanelUserRepository extends JpaRepository<PanelUser, Long> {

  Optional<PanelUser> findByEmailIgnoreCase(String email);

  List<PanelUser> findAllByDeletedFalse();

  Optional<PanelUser> findByIdAndDeletedFalse(Long id);
}
