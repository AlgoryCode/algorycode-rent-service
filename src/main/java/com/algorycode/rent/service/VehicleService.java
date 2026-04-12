package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CreateVehicleRequest;
import com.algorycode.rent.api.dto.UpdateVehicleRequest;
import com.algorycode.rent.api.dto.VehicleDto;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.location.City;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleImage;
import com.algorycode.rent.domain.vehicle.VehicleImageSlot;
import com.algorycode.rent.repository.CityRepository;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VehicleService {

  private final VehicleRepository vehicleRepository;
  private final CityRepository cityRepository;
  private final RentalRepository rentalRepository;
  private final ObjectStorageService objectStorageService;
  private static final EnumSet<VehicleImageSlot> REQUIRED_IMAGE_SLOTS =
      EnumSet.of(
          VehicleImageSlot.front,
          VehicleImageSlot.rear,
          VehicleImageSlot.left,
          VehicleImageSlot.right,
          VehicleImageSlot.interiorDash,
          VehicleImageSlot.interiorRear);

  public VehicleService(
      VehicleRepository vehicleRepository,
      CityRepository cityRepository,
      RentalRepository rentalRepository,
      ObjectStorageService objectStorageService) {
    this.vehicleRepository = vehicleRepository;
    this.cityRepository = cityRepository;
    this.rentalRepository = rentalRepository;
    this.objectStorageService = objectStorageService;
  }

  @Transactional(readOnly = true)
  public List<VehicleDto> listAll() {
    return vehicleRepository.findAll().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public VehicleDto getById(UUID id) {
    var v =
        vehicleRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
    return toDto(v);
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
    City city = findCityRequired(req.cityId());
    v.setCity(city);
    v.setCountryCode(city.getCountry().getCode());
    applyOptionalVehicleSpecs(v, req.engine(), req.seats(), req.luggage());
    validateRequiredVehicleImages(req.images());
    v = vehicleRepository.save(v);
    if (req.images() != null) {
      applyVehicleImages(v, req.images());
      v = vehicleRepository.save(v);
    }
    return toDto(v);
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

    if (req.cityId() != null) {
      City city = findCityRequired(req.cityId());
      v.setCity(city);
      v.setCountryCode(city.getCountry().getCode());
    }

    if (req.engine() != null) {
      v.setEngine(req.engine().isBlank() ? null : req.engine().trim());
    }
    if (req.seats() != null) {
      v.setSeats(req.seats());
    }
    if (req.luggage() != null) {
      v.setLuggage(req.luggage());
    }

    if (req.images() != null) {
      validateRequiredVehicleImages(req.images());
      applyVehicleImages(v, req.images());
    }

    return toDto(vehicleRepository.save(v));
  }

  /**
   * Tek görsel slotunu günceller: yeni dosyayı yükler, eski object storage kaydını siler.
   */
  @Transactional
  public VehicleDto replaceImageSlot(UUID vehicleId, VehicleImageSlot slot, String imageValue) {
    if (imageValue == null || imageValue.isBlank()) {
      throw new BadRequestException("Görsel verisi zorunludur.");
    }
    Vehicle v =
        vehicleRepository
            .findById(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

    VehicleImage existing = null;
    for (VehicleImage img : v.getImages()) {
      if (img.getSlot() == slot) {
        existing = img;
        break;
      }
    }
    String newStored =
        objectStorageService.uploadDataUrl(
            "vehicles/" + v.getId() + "/" + slot.name(), slot.name(), imageValue.trim());
    if (newStored == null || newStored.isBlank()) {
      throw new BadRequestException("Görsel yüklenemedi.");
    }
    if (existing != null) {
      String oldRef = existing.getImageUrl();
      existing.setImageUrl(newStored);
      objectStorageService.deleteObjectIfStored(oldRef);
    } else {
      VehicleImage img = new VehicleImage();
      img.setVehicle(v);
      img.setSlot(slot);
      img.setImageUrl(newStored);
      v.getImages().add(img);
    }
    return toDto(vehicleRepository.save(v));
  }

  /**
   * Tek görsel slotunu kaldırır; object storage’daki nesneyi silmeyi dener.
   */
  @Transactional
  public VehicleDto deleteImageSlot(UUID vehicleId, VehicleImageSlot slot) {
    Vehicle v =
        vehicleRepository
            .findById(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

    VehicleImage existing = null;
    for (VehicleImage img : v.getImages()) {
      if (img.getSlot() == slot) {
        existing = img;
        break;
      }
    }
    if (existing == null) {
      throw new ResourceNotFoundException("Bu slotta görsel yok: " + slot.name());
    }
    objectStorageService.deleteObjectIfStored(existing.getImageUrl());
    v.getImages().remove(existing);
    existing.setVehicle(null);
    return toDto(vehicleRepository.save(v));
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
    for (VehicleImage img : new ArrayList<>(v.getImages())) {
      objectStorageService.deleteObjectIfStored(img.getImageUrl());
    }
    vehicleRepository.delete(v);
  }

  private City findCityRequired(UUID cityId) {
    return cityRepository
        .findById(cityId)
        .orElseThrow(() -> new ResourceNotFoundException("Şehir bulunamadı: " + cityId));
  }

  private void applyOptionalVehicleSpecs(Vehicle v, String engine, Integer seats, Integer luggage) {
    v.setEngine(engine == null || engine.isBlank() ? null : engine.trim());
    v.setSeats(seats);
    v.setLuggage(luggage);
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

  private void validateRequiredVehicleImages(Map<String, String> images) {
    if (images == null) {
      throw new BadRequestException("Araç görselleri zorunludur (ön, arka, sol, sağ, kokpit, arka koltuk).");
    }
    for (VehicleImageSlot requiredSlot : REQUIRED_IMAGE_SLOTS) {
      String value = images.get(requiredSlot.name());
      if (value == null || value.isBlank()) {
        throw new BadRequestException("Araç görsellerinde " + requiredSlot.name() + " zorunludur.");
      }
    }
  }

  private void removeStoredVehicleImages(Vehicle vehicle) {
    List<VehicleImage> copy = new ArrayList<>(vehicle.getImages());
    for (VehicleImage img : copy) {
      objectStorageService.deleteObjectIfStored(img.getImageUrl());
    }
    vehicle.getImages().clear();
  }

  private void applyVehicleImages(Vehicle vehicle, Map<String, String> images) {
    removeStoredVehicleImages(vehicle);
    for (var e : images.entrySet()) {
      String imageValue = e.getValue();
      if (imageValue == null || imageValue.isBlank()) {
        continue;
      }
      final VehicleImageSlot slot;
      try {
        slot = VehicleImageSlot.valueOf(e.getKey());
      } catch (IllegalArgumentException ex) {
        throw new BadRequestException("Geçersiz görsel slotu: " + e.getKey());
      }
      String storedKey =
          objectStorageService.uploadDataUrl(
              "vehicles/" + vehicle.getId() + "/" + slot.name(),
              slot.name(),
              imageValue.trim());
      VehicleImage img = new VehicleImage();
      img.setVehicle(vehicle);
      img.setSlot(slot);
      img.setImageUrl(storedKey);
      vehicle.getImages().add(img);
    }
  }

  private VehicleDto toDto(Vehicle v) {
    Map<String, String> images = new HashMap<>();
    for (VehicleImage img : v.getImages()) {
      if (img.getSlot() == null) {
        continue;
      }
      String resolved = objectStorageService.resolvePublicUrl(img.getImageUrl());
      if (resolved == null || resolved.isBlank()) {
        continue;
      }
      images.put(img.getSlot().name(), resolved);
    }
    City city = v.getCity();
    var country = city != null ? city.getCountry() : null;

    return new VehicleDto(
        v.getId(),
        v.getPlate(),
        v.getBrand(),
        v.getModel(),
        v.getYear(),
        v.isMaintenance(),
        v.isExternal(),
        v.getExternalCompany(),
        v.getRentalDailyPrice(),
        v.isCommissionEnabled(),
        v.getCommissionRatePercent(),
        v.getCommissionBrokerFullName(),
        v.getCommissionBrokerPhone(),
        country != null ? country.getCode() : v.getCountryCode(),
        country != null ? country.getName() : null,
        city != null ? city.getId() : null,
        city != null ? city.getName() : null,
        v.getEngine(),
        v.getSeats(),
        v.getLuggage(),
        Map.copyOf(images));
  }
}
