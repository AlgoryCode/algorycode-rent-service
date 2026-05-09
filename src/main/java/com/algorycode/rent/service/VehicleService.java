package com.algorycode.rent.service;

import com.algorycode.rent.dto.CreateVehicleRequest;
import com.algorycode.rent.dto.HandoverLocationRefDto;
import com.algorycode.rent.dto.UpdateVehicleRequest;
import com.algorycode.rent.dto.VehicleDto;
import com.algorycode.rent.dto.VehicleOptionDefinitionDto;
import com.algorycode.rent.dto.VehicleOptionDefinitionRequest;
import com.algorycode.rent.exception.BadRequestException;
import com.algorycode.rent.exception.ConflictException;
import com.algorycode.rent.exception.ResourceNotFoundException;
import com.algorycode.rent.mapper.HandoverLocationMapper;
import com.algorycode.rent.entity.HandoverLocation;
import com.algorycode.rent.entity.Vehicle;
import com.algorycode.rent.entity.VehicleAllowedReturnHandover;
import com.algorycode.rent.entity.VehicleHighlight;
import com.algorycode.rent.entity.VehicleImage;
import com.algorycode.rent.entity.VehicleImageSlot;
import com.algorycode.rent.entity.VehicleOptionDefinition;
import com.algorycode.rent.entity.VehicleStatus;
import com.algorycode.rent.entity.VehicleOptionTemplate;
import com.algorycode.rent.events.VehicleCreatedImagesEvent;
import com.algorycode.rent.logging.AuditLog;
import com.algorycode.rent.repository.HandoverLocationRepository;
import com.algorycode.rent.repository.VehicleOptionTemplateRepository;
import com.algorycode.rent.repository.VehicleRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleService {
  private final VehicleRepository vehicleRepository;
  private final HandoverLocationRepository handoverLocationRepository;
  private final VehicleOptionTemplateRepository vehicleOptionTemplateRepository;
  private final ObjectStorageService objectStorageService;
  private final VehicleAvailabilityService vehicleAvailabilityService;
  private final VehicleImageService vehicleImageService;
  private final AuditLog auditLog;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final VehicleCatalogStatusService vehicleCatalogStatusService;

  @Transactional(readOnly = true)
  public List<VehicleDto> listAll() {
    return vehicleRepository.findAllByDeletedFalse().stream().map(this::toDto).toList();
  }

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
  public VehicleDto updateVehicleStatus(Long id, VehicleStatus status) {
    vehicleCatalogStatusService.updateVehicleStatus(id, status);
    return getById(id);
  }

  @Transactional
  public Long create(CreateVehicleRequest req) {
    String normalizedPlate = normalizePlate(req.plate());
    if (vehicleRepository.existsByPlateIgnoreCaseAndDeletedFalse(normalizedPlate)) {
      throw new ConflictException("Vehicle plate already exists: " + normalizedPlate);
    }
    Vehicle v = new Vehicle();
    v.setPlate(normalizedPlate);
    v.setVehicleModelId(req.vehicleModelId());
    v.setVehicleStatus(VehicleStatus.ACTIVE);
    v.setYear(req.year());
    v.setExternal(Boolean.TRUE.equals(req.external()));
    v.setExternalCompany(req.externalCompany());
    v.setRentalDailyPrice(req.rentalDailyPrice());
    v.setCommissionEnabled(Boolean.TRUE.equals(req.external()));
    v.setCommissionRatePercent(req.commissionRatePercent());
    v.setCommissionBrokerPhone(req.commissionBrokerPhone());
    v.setCountryCode(req.countryCode());
    v.setDefaultPickupHandoverLocationId(req.defaultPickupHandoverLocationId());
    replaceVehicleReturnHandovers(v, req.returnHandoverLocationIds());
    v.setEngine(req.engine());
    v.setSeats(req.seats());
    v.setLuggage(req.luggage());
    v.setFuelTypeId(req.fuelTypeId());
    v.setBodyColor(req.bodyColor());
    v.setTransmissionTypeId(req.transmissionTypeId());
    v.setBodyStyleId(req.bodyStyleId());
    replaceVehicleHighlights(v, req.highlights());
    v = vehicleRepository.save(v);

    List<VehicleOptionDefinitionRequest> merged =
        mergeOptionDefinitions(req.optionTemplateIds(), req.optionDefinitions());
    replaceVehicleOptionDefinitions(v, merged);
    v = vehicleRepository.save(v);

    if (req.images() != null && !req.images().isEmpty()) {
      applicationEventPublisher.publishEvent(
          new VehicleCreatedImagesEvent(v.getId(), Map.copyOf(req.images())));
    }

    auditLog.infoEvent("vehicle_created", Map.of("vehicleId", v.getId().toString()));
    return v.getId();
  }

  @Transactional
  public void update(Long id, UpdateVehicleRequest req) {
    Vehicle v =
        vehicleRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));

    String normalizedPlate = normalizePlate(req.plate());
    if (vehicleRepository.existsByPlateIgnoreCaseAndDeletedFalseAndIdNot(normalizedPlate, id)) {
      throw new ConflictException("Vehicle plate already exists: " + normalizedPlate);
    }
    v.setPlate(normalizedPlate);
    v.setVehicleModelId(req.vehicleModelId());
    if (req.vehicleStatus() != null) {
      v.setVehicleStatus(req.vehicleStatus());
    }
    v.setYear(req.year());
    v.setExternal(Boolean.TRUE.equals(req.external()));
    v.setExternalCompany(req.externalCompany());
    v.setRentalDailyPrice(req.rentalDailyPrice());
    v.setCountryCode(req.countryCode());
    v.setCommissionEnabled(Boolean.TRUE.equals(req.external()));
    v.setCommissionRatePercent(req.commissionRatePercent());
    v.setCommissionBrokerPhone(req.commissionBrokerPhone());
    v.setEngine(req.engine());
    v.setFuelTypeId(req.fuelTypeId());
    v.setBodyColor(req.bodyColor());
    v.setSeats(req.seats());
    v.setLuggage(req.luggage());
    v.setTransmissionTypeId(req.transmissionTypeId());
    v.setBodyStyleId(req.bodyStyleId());
    v.setDefaultPickupHandoverLocationId(req.defaultPickupHandoverLocationId());
    replaceVehicleReturnHandovers(v, req.returnHandoverLocationIds());
    replaceVehicleOptionDefinitions(
        v,
        mergeOptionDefinitions(req.optionTemplateIds(), req.optionDefinitions()));
    if (req.images() != null) {
      vehicleImageService.applyVehicleImages(v, req.images());
    }
    replaceVehicleHighlights(v, req.highlights());

    vehicleRepository.save(v);
  }

  @Transactional
  public VehicleDto replaceImageSlot(Long vehicleId, VehicleImageSlot slot, String imageValue) {
    Vehicle v = vehicleImageService.replaceImageSlot(vehicleId, slot, imageValue);
    return toDto(v);
  }

  @Transactional
  public VehicleDto deleteImageSlot(Long vehicleId, VehicleImageSlot slot) {
    Vehicle v = vehicleImageService.deleteImageSlot(vehicleId, slot);
    return toDto(v);
  }

  @Transactional
  public void delete(Long id) {
    Vehicle v =
        vehicleRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
    v.setDeleted(true);
    vehicleRepository.save(v);
    auditLog.infoEvent("vehicle_deleted", Map.of("vehicleId", v.getId().toString()));
  }

  private String normalizePlate(String plate) {
    if (plate == null || plate.isBlank()) {
      throw new BadRequestException("Vehicle plate is required");
    }
    return plate.trim().replaceAll("\\s+", " ");
  }

  private List<VehicleOptionDefinitionRequest> mergeOptionDefinitions(
      List<Long> templateIds, List<VehicleOptionDefinitionRequest> manual) {
    List<VehicleOptionDefinitionRequest> merged = new ArrayList<>();
    int lo = 0;
    List<Long> safeTemplateIds = templateIds == null ? List.of() : templateIds;
    for (Long tid : safeTemplateIds) {
      VehicleOptionTemplate t = vehicleOptionTemplateRepository.getReferenceById(tid);
      merged.add(
          new VehicleOptionDefinitionRequest(
              t.getTitle(),
              t.getDescription(),
              t.getPrice().setScale(2, RoundingMode.HALF_UP),
              t.getIcon(),
              lo++,
              true));
    }
    List<VehicleOptionDefinitionRequest> safeManual = manual == null ? List.of() : manual;
    for (VehicleOptionDefinitionRequest r : safeManual) {
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
    List<String> safeLines = lines == null ? List.of() : lines;
    for (String line : safeLines) {
      VehicleHighlight h = new VehicleHighlight();
      h.setVehicle(v);
      h.setLineOrder(v.getHighlights().size());
      h.setText(line);
      v.getHighlights().add(h);
    }
  }





  private void replaceVehicleOptionDefinitions(
      Vehicle v, List<VehicleOptionDefinitionRequest> defs) {
    for (VehicleOptionDefinitionRequest r : defs) {
      VehicleOptionDefinition option = new VehicleOptionDefinition();
      option.setVehicleId(v.getId());
      option.setTitle(r.title().trim());
      option.setDescription(r.description());
      option.setPrice(r.price().setScale(2, RoundingMode.HALF_UP));
      option.setIcon(r.icon());
      option.setLineOrder(r.lineOrder());
      option.setActive(r.active());
      v.getOptionDefinitions().add(option);
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

  private void replaceVehicleReturnHandovers(Vehicle v, List<Long> ids) {
    v.getAllowedReturnHandovers().clear();
    int order = 0;
    List<Long> safeIds = ids == null ? List.of() : ids;
    for (Long hid : safeIds) {
      HandoverLocation loc = handoverLocationRepository.getReferenceById(hid);
      VehicleAllowedReturnHandover link = new VehicleAllowedReturnHandover();
      link.setVehicle(v);
      link.setHandoverLocation(loc);
      link.setLineOrder(order++);
      v.getAllowedReturnHandovers().add(link);
    }
  }

  private VehicleDto toDto(Vehicle v) {
    Map<String, String> images = new HashMap<>();
    for (VehicleImage img : v.getImages()) {
      String resolved = objectStorageService.resolvePublicUrl(img.getImageUrl());
      if (resolved == null || resolved.isBlank()) {
        continue;
      }
      images.put(img.getSlot().name(), resolved);
    }

    List<HandoverLocationRefDto> returnRefs =
        v.getAllowedReturnHandovers().stream()
            .sorted(
                Comparator.comparingInt(VehicleAllowedReturnHandover::getLineOrder)
                    .thenComparing(
                        VehicleAllowedReturnHandover::getId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
            .map(l -> HandoverLocationMapper.toRef(l.getHandoverLocation()))
            .toList();
    HandoverLocationRefDto firstReturn = returnRefs.isEmpty() ? null : returnRefs.get(0);

    String bodyStyleLabel = v.getBodyStyleRef() != null ? v.getBodyStyleRef().getLabelTr() : null;

    String statusCode = v.getStatus().name();

    Long modelId = v.getVehicleModel() != null ? v.getVehicleModel().getId() : null;
    Long transmissionTypeId =
        v.getTransmissionTypeRef() != null ? v.getTransmissionTypeRef().getId() : null;
    Long bodyStyleId = v.getBodyStyleRef() != null ? v.getBodyStyleRef().getId() : null;
    Long fuelTypeId = v.getFuelTypeRef() != null ? v.getFuelTypeRef().getId() : null;

    return new VehicleDto(
        v.getId(),
        modelId,
        transmissionTypeId,
        bodyStyleId,
        fuelTypeId,
        v.getPlate(),
        v.getBrand(),
        v.getModel(),
        v.getYear() != null ? v.getYear() : 0,
        v.getStatus(),
        statusCode,
        v.isExternal(),
        v.getExternalCompany(),
        v.getRentalDailyPrice(),
        v.isCommissionEnabled(),
        v.getCommissionRatePercent(),
        v.getCommissionBrokerFullName(),
        v.getCommissionBrokerPhone(),
        v.getCountryCode(),
        v.getEngine(),
        v.getFuelType(),
        v.getBodyColor(),
        v.getSeats(),
        v.getLuggage(),
        v.getTransmissionTypeCode(),
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
        Map.copyOf(images));
  }
}
