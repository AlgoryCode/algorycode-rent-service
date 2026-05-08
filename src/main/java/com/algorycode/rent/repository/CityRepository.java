package com.algorycode.rent.repository;

import com.algorycode.rent.entity.City;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {

  Optional<City> findByNameIgnoreCaseAndCountry_Id(String name, Long countryId);

  List<City> findByCountry_IdOrderByNameAsc(Long countryId);
}
