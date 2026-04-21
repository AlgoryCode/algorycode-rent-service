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
import com.algorycode.rent.domain.country.Country;
import com.algorycode.rent.domain.location.City;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.domain.location.HandoverLocationKind;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleAllowedReturnHandover;
import com.algorycode.rent.domain.vehicle.VehicleBodyStyle;
import com.algorycode.rent.domain.vehicle.VehicleFuelType;
import com.algorycode.rent.domain.vehicle.VehicleTransmissionType;
import com.algorycode.rent.domain.vehicle.VehicleHighlight;
import com.algorycode.rent.domain.vehicle.VehicleImage;
import com.algorycode.rent.domain.vehicle.VehicleImageSlot;
import com.algorycode.rent.domain.vehicle.VehicleOptionDefinition;
import com.algorycode.rent.domain.vehicle.VehicleOptionTemplate;
import com.algorycode.rent.repository.CityRepository;
import com.algorycode.rent.repository.CountryRepository;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.repository.VehicleBodyStyleRepository;
import com.algorycode.rent.repository.VehicleFuelTypeRepository;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.repository.VehicleTransmissionTypeRepository;
import com.algorycode.rent.service.readmodel.FeFleetSnapshotBuilder;
import com.algorycode.rent.service.support.Text;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class VehicleService {

  private final VehicleRepository vehicleRepository;
  private final VehicleBodyStyleRepository vehicleBodyStyleRepository;
  private final VehicleFuelTypeRepository vehicleFuelTypeRepository;
  private final VehicleTransmissionTypeRepository vehicleTransmissionTypeRepository;
  private final CountryRepository countryRepository;
  private final CityRepository cityRepository;
  private final ObjectStorageService objectStorageService;
  private final HandoverLocationService handoverLocationService;
  private final VehicleOptionTemplateService vehicleOptionTemplateService;
  private final VehicleAvailabilityService vehicleAvailabilityService;
  private final VehicleImageService vehicleImageService;
  private final AuditLog auditLog;
  private final FeFleetSnapshotBuilder feFleetSnapshotBuilder;

  public VehicleService(
      VehicleRepository vehicleRepository,
      VehicleBodyStyleRepository vehicleBodyStyleRepository,
      VehicleFuelTypeRepository vehicleFuelTypeRepository,
      VehicleTransmissionTypeRepository vehicleTransmissionTypeRepository,
      CountryRepository countryRepository,
      CityRepository cityRepository,
      ObjectStorageService objectStorageService,
      HandoverLocationService handoverLocationService,
      VehicleOptionTemplateService vehicleOptionTemplateService,
      VehicleAvailabilityService vehicleAvailabilityService,
      VehicleImageService vehicleImageService,
      AuditLog auditLog,
      FeFleetSnapshotBuilder feFleetSnapshotBuilder) {
    this.vehicleRepository = vehicleRepository;
    this.vehicleBodyStyleRepository = vehicleBodyStyleRepository;
    this.vehicleFuelTypeRepository = vehicleFuelTypeRepository;
    this.vehicleTransmissionTypeRepository = vehicleTransmissionTypeRepository;
    this.countryRepository = countryRepository;
    this.cityRepository = cityRepository;
    this.objectStorageService = objectStorageService;
    this.handoverLocationService = handoverLocationService;
    this.vehicleOptionTemplateService = vehicleOptionTemplateService;
    this.vehicleAvailabilityService = vehicleAvailabilityService;
    this.vehicleImageService = vehicleImageService;
    this.auditLog = auditLog;
    this.feFleetSnapshotBuilder = feFleetSnapshotBuilder;
  }

  @Transactional(readOnly = true)
  public List<VehicleDto> listAll() {
    return vehicleRepository.findAllByDeletedFalse().stream().map(this::toDto).toList();
  }

  /**
   * Kiralama uygunluğu: iptal olmayan kiralamalarla çakışma yok + bitiş gününün ertesi takvim günü
   * tamamen boş (araç hazırlık tamponu). İsteğe bağlı alış ({@code defaultPickup}) ve izinli teslim
   * noktalarına göre süzülür.
   */
  @Transactional(readOnly = true)
  public List<VehicleDto> listWithAvailabilityFilter(
      LocalDate availableFrom,
      LocalDate availableTo,
      Long pickupHandoverLocationId,
      Long returnHandoverLocationId,
      boolean includePartialAvailability) {
    return vehicleAvailabilityService
        .listVehiclesMatchingAvailability(
            availableFrom,
            availableTo,
            pickupHandoverLocationId,
            returnHandoverLocationId,
            includePartialAvailability)
        .stream()
        .map(this::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public VehicleDto getById(Long id) {
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
    applyCountryAndOptionalCity(v, req.countryCode(), req.cityId());
    v.setDefaultPickupHandoverLocation(
        handoverLocationService.requireForAssignment(
            req.defaultPickupHandoverLocationId(), HandoverLocationKind.PICKUP));
    replaceVehicleReturnHandovers(v, resolveCreateReturnHandoverIds(req));
    applyOptionalVehicleSpecs(v, req.engine(), req.seats(), req.luggage());
    v.setFuelType(resolveFuelTypeOrNull(req.fuelType()));
    v.setBodyColor(Text.trimOrNull(req.bodyColor()));
    v.setTransmissionType(resolveTransmissionTypeOrNull(req.transmissionType()));
    v.setBodyStyleCode(resolveBodyStyleCodeOrNull(req.bodyStyleCode()));
    replaceVehicleHighlights(v, req.highlights());
    vehicleImageService.validateRequiredVehicleImages(req.images());
    v = vehicleRepository.save(v);
    List<VehicleOptionDefinitionRequest> merged =
        mergeOptionDefinitions(req.optionTemplateIds(), req.optionDefinitions());
    if (!merged.isEmpty()) {
      replaceVehicleOptionDefinitions(v, merged);
      v = vehicleRepository.save(v);
    }
    if (req.images() != null) {
      vehicleImageService.applyVehicleImages(v, req.images());
      v = vehicleRepository.save(v);
    }
    persistFleetSnapshot(v);
    v = vehicleRepository.save(v);
    auditLog.infoEvent("vehicle_created", Map.of("vehicleId", v.getId().toString()));
    return toDto(v);
  }

  @Transactional
  public VehicleDto update(Long id, UpdateVehicleRequest req) {
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
    if (req.fuelType() != null) {
      v.setFuelType(resolveFuelTypeOrNull(req.fuelType()));
    }
    if (req.bodyColor() != null) {
      v.setBodyColor(Text.trimOrNull(req.bodyColor()));
    }
    if (req.seats() != null) {
      v.setSeats(req.seats());
    }
    if (req.luggage() != null) {
      v.setLuggage(req.luggage());
    }
    if (req.transmissionType() != null) {
      v.setTransmissionType(resolveTransmissionTypeOrNull(req.transmissionType()));
    }
    if (req.bodyStyleCode() != null) {
      v.setBodyStyleCode(resolveBodyStyleCodeOrNull(req.bodyStyleCode()));
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
      vehicleImageService.validateRequiredVehicleImages(req.images());
      vehicleImageService.applyVehicleImages(v, req.images());
    }

    if (req.highlights() != null) {
      replaceVehicleHighlights(v, req.highlights());
    }

    Vehicle saved = vehicleRepository.save(v);
    persistFleetSnapshot(saved);
    return toDto(vehicleRepository.save(saved));
  }

  /**
   * Tek görsel slotunu günceller: yeni dosyayı yükler, eski object storage kaydını siler.
   */
  @Transactional
  public VehicleDto replaceImageSlot(Long vehicleId, VehicleImageSlot slot, String imageValue) {
    Vehicle v = vehicleImageService.replaceImageSlot(vehicleId, slot, imageValue);
    persistFleetSnapshot(v);
    return toDto(vehicleRepository.save(v));
  }

  /**
   * Tek görsel slotunu kaldırır; object storage’daki nesneyi silmeyi dener.
   */
  @Transactional
  public VehicleDto deleteImageSlot(Long vehicleId, VehicleImageSlot slot) {
    Vehicle v = vehicleImageService.deleteImageSlot(vehicleId, slot);
    persistFleetSnapshot(v);
    return toDto(vehicleRepository.save(v));
  }

  @Transactional
  public void delete(Long id) {
    Vehicle v =
        vehicleRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
    v.setDeleted(true);
    vehicleRepository.save(v);
  }

  private List<VehicleOptionDefinitionRequest> mergeOptionDefinitions(
      List<Long> templateIds, List<VehicleOptionDefinitionRequest> manual) {
    List<VehicleOptionDefinitionRequest> merged = new ArrayList<>();
    int lo = 0;
    List<Long> tids = templateIds != null ? templateIds : List.of();
    for (Long tid : tids) {
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

  private static List<Long> resolveCreateReturnHandoverIds(CreateVehicleRequest req) {
    if (req.returnHandoverLocationIds() != null && !req.returnHandoverLocationIds().isEmpty()) {
      return req.returnHandoverLocationIds();
    }
    if (req.defaultReturnHandoverLocationId() != null) {
      return List.of(req.defaultReturnHandoverLocationId());
    }
    return List.of();
  }

  private void replaceVehicleReturnHandovers(Vehicle v, List<Long> ids) {
    v.getAllowedReturnHandovers().clear();
    if (ids == null || ids.isEmpty()) {
      return;
    }
    LinkedHashSet<Long> seen = new LinkedHashSet<>();
    for (Long hid : ids) {
      if (hid != null) {
        seen.add(hid);
      }
    }
    int order = 0;
    for (Long hid : seen) {
      HandoverLocation loc =
          handoverLocationService.requireForAssignment(hid, HandoverLocationKind.RETURN);
      VehicleAllowedReturnHandover link = new VehicleAllowedReturnHandover();
      link.setVehicle(v);
      link.setHandoverLocation(loc);
      link.setLineOrder(order++);
      v.getAllowedReturnHandovers().add(link);
    }
  }

  private void applyCountryAndOptionalCity(Vehicle v, String countryCodeRaw, Long cityId) {
    if (countryCodeRaw == null || countryCodeRaw.isBlank()) {
      throw new BadRequestException("Ülke kodu zorunludur.");
    }
    String code = countryCodeRaw.trim().toUpperCase();
    if (code.length() != 2) {
      throw new BadRequestException("Ülke kodu iki harfli ISO kod olmalıdır.");
    }
    Country country =
        countryRepository
            .findByCodeIgnoreCase(code)
            .orElseThrow(() -> new BadRequestException("Kayıtlı ülke bulunamadı: " + code));
    v.setCountryCode(country.getCode());
    if (cityId != null) {
      City city = findCityRequired(cityId);
      if (!city.getCountry().getId().equals(country.getId())) {
        throw new BadRequestException("Seçilen şehir, seçilen ülkeye ait değil.");
      }
      v.setCity(city);
    } else {
      v.setCity(null);
    }
  }

  private City findCityRequired(Long cityId) {
    return cityRepository
        .findById(cityId)
        .orElseThrow(() -> new ResourceNotFoundException("Şehir bulunamadı: " + cityId));
  }

  private String resolveBodyStyleCodeOrNull(String raw) {
    String c = Text.trimOrNull(raw);
    if (c == null) {
      return null;
    }
    return vehicleBodyStyleRepository
        .findByCodeIgnoreCase(c)
        .map(VehicleBodyStyle::getCode)
        .orElseThrow(() -> new BadRequestException("Geçersiz araç türü: " + raw));
  }

  private String resolveFuelTypeOrNull(String raw) {
    String t = Text.trimOrNull(raw);
    if (t == null) {
      return null;
    }
    return vehicleFuelTypeRepository
        .findByCodeIgnoreCase(t)
        .map(VehicleFuelType::getCode)
        .orElseThrow(() -> new BadRequestException("Geçersiz yakıt türü: " + raw));
  }

  private String resolveTransmissionTypeOrNull(String raw) {
    String t = Text.trimOrNull(raw);
    if (t == null) {
      return null;
    }
    return vehicleTransmissionTypeRepository
        .findByCodeIgnoreCase(t)
        .map(VehicleTransmissionType::getCode)
        .orElseThrow(() -> new BadRequestException("Geçersiz vites türü: " + raw));
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

  private void persistFleetSnapshot(Vehicle v) {
    v.setFeFleetSnapshot(feFleetSnapshotBuilder.build(v));
  }

  private JsonNode fleetSnapshotForResponse(Vehicle v) {
    if (v.getFeFleetSnapshot() != null) {
      return v.getFeFleetSnapshot();
    }
    return feFleetSnapshotBuilder.build(v);
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

    String bodyStyleLabel =
        v.getBodyStyleCode() == null || v.getBodyStyleCode().isBlank()
            ? null
            : vehicleBodyStyleRepository
                .findByCodeIgnoreCase(v.getBodyStyleCode().trim())
                .map(VehicleBodyStyle::getLabelTr)
                .orElse(null);

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
        v.getFuelType(),
        v.getBodyColor(),
        v.getSeats(),
        v.getLuggage(),
        v.getTransmissionType(),
        v.getBodyStyleCode(),
        bodyStyleLabel,
        HandoverLocationMapper.toRef(v.getDefaultPickupHandoverLocation()),
        firstReturn,
        returnRefs,
        mapOptionDefinitions(v),
        v.getHighlights().stream()
            .sorted((a, b) -> Integer.compare(a.getLineOrder(), b.getLineOrder()))
            .map(VehicleHighlight::getText)
            .toList(),
        Map.copyOf(images),
        fleetSnapshotForResponse(v));
  }
}
