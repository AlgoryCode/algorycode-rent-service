package com.algorycode.rent.repository;

import com.algorycode.rent.entity.Country;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {

  Optional<Country> findByCodeIgnoreCase(String code);
}
