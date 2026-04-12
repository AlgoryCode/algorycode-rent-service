package com.algorycode.rent.repository;

import com.algorycode.rent.domain.location.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {

  Optional<City> findByNameIgnoreCaseAndCountry_Id(String name, UUID countryId);

  List<City> findByCountry_IdOrderByNameAsc(UUID countryId);
}
