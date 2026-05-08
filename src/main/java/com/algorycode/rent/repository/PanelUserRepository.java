package com.algorycode.rent.repository;

import com.algorycode.rent.entity.PanelUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PanelUserRepository extends JpaRepository<PanelUser, Long> {

  Optional<PanelUser> findByEmailIgnoreCase(String email);

  List<PanelUser> findAllByDeletedFalse();

  Optional<PanelUser> findByIdAndDeletedFalse(Long id);
}
