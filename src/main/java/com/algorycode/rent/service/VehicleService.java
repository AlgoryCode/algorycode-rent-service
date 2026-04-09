package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CreateVehicleRequest;
import com.algorycode.rent.api.dto.UpdateVehicleRequest;
import com.algorycode.rent.api.dto.VehicleDto;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.api.mapper.VehicleMapper;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleImage;
import com.algorycode.rent.domain.vehicle.VehicleImageSlot;
import com.algorycode.rent.repository.CountryRepository;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VehicleService {

  private final VehicleRepository vehicleRepository;
  private final CountryRepository countryRepository;
  private final RentalRepository rentalRepository;

  public VehicleService(
      VehicleRepository vehicleRepository,
      CountryRepository countryRepository,
      RentalRepository rentalRepository) {
    this.vehicleRepository = vehicleRepository;
    this.countryRepository = countryRepository;
    this.rentalRepository = rentalRepository;
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
    BigDecimal rentalDailyPrice = req.rentalDailyPrice();
    if (rentalDailyPrice == null || rentalDailyPrice.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BadRequestException("Günlük kiralama fiyatı sıfırdan büyük olmalıdır.");
    }
    v.setRentalDailyPrice(rentalDailyPrice);

    applyCommissionRules(v, req.external(), req.commissionRatePercent(), req.commissionBrokerPhone());
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

  @Transactional
  public VehicleDto update(UUID id, UpdateVehicleRequest req) {
    Vehicle v =
        vehicleRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));

    if (req.plate() != null) {
      String plate = req.plate().trim().replaceAll("\\s+", " ");
      if (plate.isBlank()) {
        throw new BadRequestException("Plaka boş olamaz.");
      }
      if (!plate.equalsIgnoreCase(v.getPlate()) && vehicleRepository.existsByPlateIgnoreCase(plate)) {
        throw new ConflictException("Bu plaka zaten kayıtlı.");
      }
      v.setPlate(plate);
    }
    if (req.brand() != null) v.setBrand(req.brand().trim());
    if (req.model() != null) v.setModel(req.model().trim());
    if (req.year() != null) v.setYear(req.year());
    if (req.maintenance() != null) v.setMaintenance(req.maintenance());

    boolean nextExternal = req.external() != null ? req.external() : v.isExternal();
    v.setExternal(nextExternal);

    String nextExternalCompany = req.externalCompany() != null ? req.externalCompany() : v.getExternalCompany();
    if (nextExternal) {
      if (nextExternalCompany == null || nextExternalCompany.isBlank()) {
        throw new BadRequestException("Harici araç için firma adı zorunludur.");
      }
      v.setExternalCompany(nextExternalCompany.trim());
    } else {
      v.setExternalCompany(null);
    }

    if (req.rentalDailyPrice() != null) {
      if (req.rentalDailyPrice().compareTo(BigDecimal.ZERO) <= 0) {
        throw new BadRequestException("Günlük kiralama fiyatı sıfırdan büyük olmalıdır.");
      }
      v.setRentalDailyPrice(req.rentalDailyPrice());
    }

    BigDecimal nextRate = req.commissionRatePercent() != null ? req.commissionRatePercent() : v.getCommissionRatePercent();
    String nextPhone = req.commissionBrokerPhone() != null ? req.commissionBrokerPhone() : v.getCommissionBrokerPhone();
    applyCommissionRules(v, nextExternal, nextRate, nextPhone);

    if (req.countryCode() != null) {
      if (req.countryCode().isBlank()) {
        v.setCountryCode(null);
      } else {
        String code = req.countryCode().trim().toUpperCase();
        countryRepository
            .findByCodeIgnoreCase(code)
            .orElseThrow(() -> new ResourceNotFoundException("Ülke bulunamadı: " + code));
        v.setCountryCode(code);
      }
    }

    if (req.images() != null) {
      v.getImages().clear();
      for (var e : req.images().entrySet()) {
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

  @Transactional
  public void delete(UUID id) {
    Vehicle v =
        vehicleRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
    if (rentalRepository.existsByVehicle_Id(id)) {
      throw new ConflictException("Bu araca ait kiralama kayıtları olduğu için silinemez.");
    }
    vehicleRepository.delete(v);
  }

  private void applyCommissionRules(Vehicle v, boolean external, BigDecimal commissionRatePercent, String brokerPhone) {
    v.setCommissionEnabled(external);
    if (external) {
      if (commissionRatePercent == null
          || commissionRatePercent.compareTo(BigDecimal.ZERO) <= 0
          || commissionRatePercent.compareTo(new BigDecimal("100")) > 0) {
        throw new BadRequestException("Harici araçta komisyon oranı 0 ile 100 arasında zorunludur.");
      }
      v.setCommissionRatePercent(commissionRatePercent);
      v.setCommissionBrokerFullName(null);
      v.setCommissionBrokerPhone(
          brokerPhone == null || brokerPhone.isBlank() ? null : brokerPhone.trim());
    } else {
      v.setCommissionRatePercent(null);
      v.setCommissionBrokerFullName(null);
      v.setCommissionBrokerPhone(null);
    }
  }
}
