package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CreateVehicleRequest;
import com.algorycode.rent.api.dto.VehicleDto;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.api.mapper.VehicleMapper;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleImage;
import com.algorycode.rent.domain.vehicle.VehicleImageSlot;
import com.algorycode.rent.repository.CountryRepository;
import com.algorycode.rent.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VehicleService {

  private final VehicleRepository vehicleRepository;
  private final CountryRepository countryRepository;

  public VehicleService(VehicleRepository vehicleRepository, CountryRepository countryRepository) {
    this.vehicleRepository = vehicleRepository;
    this.countryRepository = countryRepository;
  }

  @Transactional(readOnly = true)
  public List<VehicleDto> listAll() {
    return vehicleRepository.findAll().stream().map(VehicleMapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public VehicleDto getById(UUID id) {
    var v =
        vehicleRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
    return VehicleMapper.toDto(v);
  }

  @Transactional
  public VehicleDto create(CreateVehicleRequest req) {
    String plate = req.plate().trim().replaceAll("\\s+", " ");
    if (vehicleRepository.existsByPlateIgnoreCase(plate)) {
      throw new ConflictException("Bu plaka zaten kayıtlı.");
    }
    Vehicle v = new Vehicle();
    v.setPlate(plate);
    v.setBrand(req.brand().trim());
    v.setModel(req.model().trim());
    v.setYear(req.year());
    v.setMaintenance(req.maintenance());
    v.setExternal(req.external());
    if (req.external()) {
      if (req.externalCompany() == null || req.externalCompany().isBlank()) {
        throw new BadRequestException("Harici araç için firma adı zorunludur.");
      }
      v.setExternalCompany(req.externalCompany().trim());
    } else {
      v.setExternalCompany(null);
    }
    BigDecimal defaultCommission = req.defaultCommissionAmount();
    if (defaultCommission != null) {
      if (defaultCommission.compareTo(BigDecimal.ZERO) < 0) {
        throw new BadRequestException("Komisyon tutarı negatif olamaz.");
      }
      v.setDefaultCommissionAmount(defaultCommission.setScale(2, RoundingMode.HALF_UP));
    }
    String cc = req.countryCode();
    if (cc != null && !cc.isBlank()) {
      String code = cc.trim().toUpperCase();
      countryRepository
          .findByCodeIgnoreCase(code)
          .orElseThrow(() -> new ResourceNotFoundException("Ülke bulunamadı: " + code));
      v.setCountryCode(code);
    }
    Map<String, String> images = req.images();
    if (images != null) {
      for (var e : images.entrySet()) {
        String url = e.getValue();
        if (url == null || url.isBlank()) {
          continue;
        }
        final VehicleImageSlot slot;
        try {
          slot = VehicleImageSlot.valueOf(e.getKey());
        } catch (IllegalArgumentException ex) {
          throw new BadRequestException("Geçersiz görsel slotu: " + e.getKey());
        }
        VehicleImage img = new VehicleImage();
        img.setVehicle(v);
        img.setSlot(slot);
        img.setImageUrl(url.trim());
        v.getImages().add(img);
      }
    }
    return VehicleMapper.toDto(vehicleRepository.save(v));
  }
}
