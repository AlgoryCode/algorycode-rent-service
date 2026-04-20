package com.algorycode.rent.repository;

import com.algorycode.rent.domain.country.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {

  Optional<Country> findByCodeIgnoreCase(String code);
}
