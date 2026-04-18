package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CreateVehicleRequest;
import com.algorycode.rent.api.dto.HandoverLocationRefDto;
import com.algorycode.rent.api.dto.UpdateVehicleRequest;
import com.algorycode.rent.api.dto.VehicleDto;
import com.algorycode.rent.api.dto.VehicleOptionDefinitionDto;
import com.algorycode.rent.api.dto.VehicleOptionDefinitionRequest;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.api.mapper.HandoverLocationMapper;
import com.algorycode.rent.domain.location.City;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.domain.location.HandoverLocationKind;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleAllowedReturnHandover;
import com.algorycode.rent.domain.vehicle.VehicleHighlight;
import com.algorycode.rent.domain.vehicle.VehicleImage;
import com.algorycode.rent.domain.vehicle.VehicleImageSlot;
import com.algorycode.rent.domain.vehicle.VehicleOptionDefinition;
import com.algorycode.rent.domain.vehicle.VehicleOptionTemplate;
import com.algorycode.rent.repository.CityRepository;
import com.algorycode.rent.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VehicleService {

  private final VehicleRepository vehicleRepository;
  private final CityRepository cityRepository;
  private final ObjectStorageService objectStorageService;
  private final HandoverLocationService handoverLocationService;
  private final VehicleOptionTemplateService vehicleOptionTemplateService;
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
      ObjectStorageService objectStorageService,
      HandoverLocationService handoverLocationService,
      VehicleOptionTemplateService vehicleOptionTemplateService) {
    this.vehicleRepository = vehicleRepository;
    this.cityRepository = cityRepository;
    this.objectStorageService = objectStorageService;
    this.handoverLocationService = handoverLocationService;
    this.vehicleOptionTemplateService = vehicleOptionTemplateService;
  }

  @Transactional(readOnly = true)
  public List<VehicleDto> listAll() {
    return vehicleRepository.findAllByDeletedFalse().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public VehicleDto getById(UUID id) {
    var v =
        vehicleRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
    return toDto(v);
  }

  @Transactional
  public VehicleDto create(CreateVehicleRequest req) {
    String plate = req.plate().trim().replaceAll("\\s+", " ");
    if (vehicleRepository.existsByPlateIgnoreCaseAndDeletedFalse(plate)) {
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
    v.setDefaultPickupHandoverLocation(
        handoverLocationService.requireForAssignment(
            req.defaultPickupHandoverLocationId(), HandoverLocationKind.PICKUP));
    replaceVehicleReturnHandovers(v, resolveCreateReturnHandoverIds(req));
    applyOptionalVehicleSpecs(v, req.engine(), req.seats(), req.luggage());
    replaceVehicleHighlights(v, req.highlights());
    validateRequiredVehicleImages(req.images());
    v = vehicleRepository.save(v);
    List<VehicleOptionDefinitionRequest> merged =
        mergeOptionDefinitions(req.optionTemplateIds(), req.optionDefinitions());
    if (!merged.isEmpty()) {
      replaceVehicleOptionDefinitions(v, merged);
      v = vehicleRepository.save(v);
    }
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
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));

    if (req.plate() != null) {
      String plate = req.plate().trim().replaceAll("\\s+", " ");
      if (plate.isBlank()) {
        throw new BadRequestException("Plaka boş olamaz.");
      }
      if (!plate.equalsIgnoreCase(v.getPlate())
          && vehicleRepository.existsByPlateIgnoreCaseAndDeletedFalseAndIdNot(plate, id)) {
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

    if (req.defaultPickupHandoverLocationId() != null) {
      v.setDefaultPickupHandoverLocation(
          handoverLocationService.requireForAssignment(
              req.defaultPickupHandoverLocationId(), HandoverLocationKind.PICKUP));
    }
    if (req.returnHandoverLocationIds() != null) {
      replaceVehicleReturnHandovers(v, req.returnHandoverLocationIds());
    } else if (req.defaultReturnHandoverLocationId() != null) {
      replaceVehicleReturnHandovers(v, List.of(req.defaultReturnHandoverLocationId()));
    }
    if (req.optionTemplateIds() != null || req.optionDefinitions() != null) {
      List<VehicleOptionDefinitionRequest> merged =
          mergeOptionDefinitions(
              req.optionTemplateIds() != null ? req.optionTemplateIds() : List.of(),
              req.optionDefinitions() != null ? req.optionDefinitions() : List.of());
      replaceVehicleOptionDefinitions(v, merged);
    }

    if (req.images() != null) {
      validateRequiredVehicleImages(req.images());
      applyVehicleImages(v, req.images());
    }

    if (req.highlights() != null) {
      replaceVehicleHighlights(v, req.highlights());
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
            .findByIdAndDeletedFalse(vehicleId)
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
            .findByIdAndDeletedFalse(vehicleId)
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
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
    v.setDeleted(true);
    vehicleRepository.save(v);
  }

  private List<VehicleOptionDefinitionRequest> mergeOptionDefinitions(
      List<UUID> templateIds, List<VehicleOptionDefinitionRequest> manual) {
    List<VehicleOptionDefinitionRequest> merged = new ArrayList<>();
    int lo = 0;
    List<UUID> tids = templateIds != null ? templateIds : List.of();
    for (UUID tid : tids) {
      VehicleOptionTemplate t = vehicleOptionTemplateService.requireActive(tid);
      merged.add(
          new VehicleOptionDefinitionRequest(
              t.getTitle(),
              t.getDescription(),
              t.getPrice().setScale(2, RoundingMode.HALF_UP),
              t.getIcon(),
              lo++,
              true));
    }
    List<VehicleOptionDefinitionRequest> man = manual != null ? manual : List.of();
    for (VehicleOptionDefinitionRequest r : man) {
      merged.add(
          new VehicleOptionDefinitionRequest(
              r.title(),
              r.description(),
              r.price().setScale(2, RoundingMode.HALF_UP),
              r.icon(),
              lo++,
              r.active() == null || Boolean.TRUE.equals(r.active())));
    }
    return merged;
  }

  private void replaceVehicleHighlights(Vehicle v, List<String> lines) {
    v.getHighlights().clear();
    if (lines == null || lines.isEmpty()) {
      return;
    }
    int order = 0;
    for (String line : lines) {
      if (line == null || line.isBlank()) {
        continue;
      }
      VehicleHighlight h = new VehicleHighlight();
      h.setVehicle(v);
      h.setLineOrder(order++);
      h.setText(line.trim());
      v.getHighlights().add(h);
    }
  }

  private void replaceVehicleOptionDefinitions(Vehicle v, List<VehicleOptionDefinitionRequest> defs) {
    v.getOptionDefinitions().clear();
    for (VehicleOptionDefinitionRequest r : defs) {
      VehicleOptionDefinition e = new VehicleOptionDefinition();
      e.setVehicle(v);
      e.setTitle(r.title().trim());
      e.setDescription(
          r.description() != null && !r.description().isBlank() ? r.description().trim() : null);
      e.setPrice(r.price().setScale(2, RoundingMode.HALF_UP));
      e.setIcon(r.icon() != null && !r.icon().isBlank() ? r.icon().trim() : null);
      e.setLineOrder(r.lineOrder());
      e.setActive(r.active() == null || Boolean.TRUE.equals(r.active()));
      v.getOptionDefinitions().add(e);
    }
  }

  private static List<VehicleOptionDefinitionDto> mapOptionDefinitions(Vehicle v) {
    return v.getOptionDefinitions().stream()
        .map(
            d ->
                new VehicleOptionDefinitionDto(
                    d.getId(),
                    d.getTitle(),
                    d.getDescription(),
                    d.getPrice(),
                    d.getIcon(),
                    d.getLineOrder(),
                    d.isActive()))
        .toList();
  }

  private static List<UUID> resolveCreateReturnHandoverIds(CreateVehicleRequest req) {
    if (req.returnHandoverLocationIds() != null && !req.returnHandoverLocationIds().isEmpty()) {
      return req.returnHandoverLocationIds();
    }
    if (req.defaultReturnHandoverLocationId() != null) {
      return List.of(req.defaultReturnHandoverLocationId());
    }
    return List.of();
  }

  private void replaceVehicleReturnHandovers(Vehicle v, List<UUID> ids) {
    v.getAllowedReturnHandovers().clear();
    if (ids == null || ids.isEmpty()) {
      return;
    }
    LinkedHashSet<UUID> seen = new LinkedHashSet<>();
    for (UUID hid : ids) {
      if (hid != null) {
        seen.add(hid);
      }
    }
    int order = 0;
    for (UUID hid : seen) {
      HandoverLocation loc =
          handoverLocationService.requireForAssignment(hid, HandoverLocationKind.RETURN);
      VehicleAllowedReturnHandover link = new VehicleAllowedReturnHandover();
      link.setVehicle(v);
      link.setHandoverLocation(loc);
      link.setLineOrder(order++);
      v.getAllowedReturnHandovers().add(link);
    }
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

    List<HandoverLocationRefDto> returnRefs =
        v.getAllowedReturnHandovers().stream()
            .sorted(
                Comparator.comparingInt(VehicleAllowedReturnHandover::getLineOrder)
                    .thenComparing(
                        VehicleAllowedReturnHandover::getId, Comparator.nullsLast(Comparator.naturalOrder())))
            .map(l -> HandoverLocationMapper.toRef(l.getHandoverLocation()))
            .toList();
    HandoverLocationRefDto firstReturn = returnRefs.isEmpty() ? null : returnRefs.get(0);

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
        HandoverLocationMapper.toRef(v.getDefaultPickupHandoverLocation()),
        firstReturn,
        returnRefs,
        mapOptionDefinitions(v),
        v.getHighlights().stream()
            .sorted((a, b) -> Integer.compare(a.getLineOrder(), b.getLineOrder()))
            .map(VehicleHighlight::getText)
            .toList(),
        Map.copyOf(images));
  }
}
