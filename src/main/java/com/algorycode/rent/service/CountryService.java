package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CountryDto;
import com.algorycode.rent.api.dto.CreateCountryRequest;
import com.algorycode.rent.api.dto.UpdateCountryColorRequest;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.api.mapper.CountryMapper;
import com.algorycode.rent.domain.country.Country;
import com.algorycode.rent.repository.CountryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CountryService {

  private final CountryRepository countryRepository;

  @Transactional(readOnly = true)
  public List<CountryDto> listAll() {
    return countryRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
        .map(CountryMapper::toDto)
        .toList();
  }

  @Transactional
  public CountryDto create(CreateCountryRequest body) {
    String code = body.code().trim().toUpperCase();
    if (countryRepository.findByCodeIgnoreCase(code).isPresent()) {
      throw new ConflictException("Bu ülke kodu zaten kayıtlı.");
    }
    Country country = new Country();
    country.setCode(code);
    country.setName(body.name().trim());
    country.setColorCode(body.colorCode().trim().toUpperCase());
    return CountryMapper.toDto(countryRepository.save(country));
  }

  @Transactional
  public CountryDto updateColor(Long id, UpdateCountryColorRequest body) {
    Country country =
        countryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ülke bulunamadı: " + id));
    country.setColorCode(body.colorCode().trim().toUpperCase());
    return CountryMapper.toDto(countryRepository.save(country));
  }
}
