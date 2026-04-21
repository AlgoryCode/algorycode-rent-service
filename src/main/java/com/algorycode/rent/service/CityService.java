package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CityDto;
import com.algorycode.rent.api.dto.CreateCityRequest;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.api.mapper.CityMapper;
import com.algorycode.rent.domain.location.City;
import com.algorycode.rent.repository.CityRepository;
import com.algorycode.rent.repository.CountryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CityService {

  private final CityRepository cityRepository;
  private final CountryRepository countryRepository;

  public CityService(CityRepository cityRepository, CountryRepository countryRepository) {
    this.cityRepository = cityRepository;
    this.countryRepository = countryRepository;
  }

  @Transactional(readOnly = true)
  public List<CityDto> listAll(Long countryId) {
    var rows =
        countryId == null
            ? cityRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
            : cityRepository.findByCountry_IdOrderByNameAsc(countryId);
    return rows.stream().map(CityMapper::toDto).toList();
  }

  @Transactional
  public CityDto create(CreateCityRequest req) {
    var country =
        countryRepository
            .findById(req.countryId())
            .orElseThrow(() -> new ResourceNotFoundException("Ülke bulunamadı: " + req.countryId()));

    String name = req.name().trim();
    if (cityRepository.findByNameIgnoreCaseAndCountry_Id(name, req.countryId()).isPresent()) {
      throw new ConflictException("Bu şehir bu ülke için zaten kayıtlı.");
    }

    City city = new City();
    city.setName(name);
    city.setCountry(country);

    return CityMapper.toDto(cityRepository.save(city));
  }
}
