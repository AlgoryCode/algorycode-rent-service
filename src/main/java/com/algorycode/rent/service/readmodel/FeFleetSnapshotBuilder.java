package com.algorycode.rent.service.readmodel;

import com.algorycode.rent.api.mapper.HandoverLocationMapper;
import com.algorycode.rent.config.AppRentalRequestProperties;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleAllowedReturnHandover;
import com.algorycode.rent.domain.vehicle.VehicleImage;
import com.algorycode.rent.domain.vehicle.VehicleHighlight;
import com.algorycode.rent.domain.vehicle.VehicleOptionDefinition;
import com.algorycode.rent.service.ObjectStorageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * user-fe {@code parseFeFleetSnapshot} ile uyumlu vitrin paketi (TRY günlük fiyat, handover UUID string).
 */
@Component
public class FeFleetSnapshotBuilder {

  private static final String FALLBACK_IMAGE =
      "https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=1200&q=80";

  private final ObjectStorageService objectStorageService;
  private final AppRentalRequestProperties rentalRequestProperties;

  public FeFleetSnapshotBuilder(
      ObjectStorageService objectStorageService, AppRentalRequestProperties rentalRequestProperties) {
    this.objectStorageService = objectStorageService;
    this.rentalRequestProperties = rentalRequestProperties;
  }

  public JsonNode build(Vehicle v) {
    JsonNodeFactory f = JsonNodeFactory.instance;
    ObjectNode root = f.objectNode();
    root.put("id", String.valueOf(v.getId()));
    root.put("brand", nz(v.getBrand()));
    root.put("name", (nz(v.getBrand()) + " " + nz(v.getModel())).trim());
    root.put("category", categoryLabel(v));
    root.set("specs", specsArray(v, f));
    root.put("transmission", feTransmission(v.getTransmissionType()));
    root.put("seats", v.getSeats() != null && v.getSeats() > 0 ? v.getSeats() : 5);
    root.put("fuel", feFuel(v.getFuelType()));
    root.put("year", v.getYear() != null ? v.getYear() : 2024);
    root.put("engine", v.getEngine() != null && !v.getEngine().isBlank() ? v.getEngine().trim() : "—");
    root.put("powerKw", 0);
    root.put("luggage", v.getLuggage() != null && v.getLuggage() >= 0 ? v.getLuggage() : 450);
    root.put("co2", "—");
    String image = pickPrimaryImage(v);
    root.put("image", image);
    ArrayNode gallery = f.arrayNode();
    gallery.add(image);
    root.set("gallery", gallery);
    root.put("imageAlt", root.get("name").asText());
    if (v.isExternal()) {
      root.put("badge", "Partner");
    }
    root.put(
        "description",
        nz(v.getBrand()) + " " + nz(v.getModel()) + " için güncel araç kaydı (rent API).");
    root.set(
        "highlights",
        stringArray(
            v.getHighlights().stream()
                .sorted(Comparator.comparingInt(VehicleHighlight::getLineOrder))
                .map(VehicleHighlight::getText)
                .toList(),
            f));
    ArrayNode included = f.arrayNode();
    included.add("Standart sigorta (detay ofiste)");
    included.add("7/24 destek");
    root.set("included", included);
    ArrayNode notIncluded = f.arrayNode();
    notIncluded.add("Yakıt");
    notIncluded.add("Otoyol / köprü geçişleri");
    root.set("notIncluded", notIncluded);
    root.put("depositHint", 15000);
    root.put(
        "garageLocation",
        "İstanbul, Maslak — Hazırlık noktası A · Filo garajı (demo). Araç bu noktadan veya anlaşmalı ofisten teslim edilir.");
    HandoverLocation defaultPickup = v.getDefaultPickupHandoverLocation();
    if (defaultPickup != null) {
      root.put("defaultPickupHandoverLocationId", String.valueOf(defaultPickup.getId()));
      root.put("defaultPickupHandoverName", defaultPickup.getName());
    }
    ArrayNode returns = returnBookingArray(v, f);
    ObjectNode pickupBooking = handoverBooking(f, defaultPickup);
    if (!returns.isEmpty()) {
      root.set("returnHandoversForBooking", returns);
      HandoverLocation firstReturn = firstReturnLocation(v);
      if (firstReturn != null) {
        root.put("defaultReturnHandoverLocationId", String.valueOf(firstReturn.getId()));
        root.put("defaultReturnHandoverName", firstReturn.getName());
      }
      if (pickupBooking != null) {
        root.set("pickupHandoverForBooking", pickupBooking);
      }
    } else if (pickupBooking != null) {
      root.set("pickupHandoverForBooking", pickupBooking);
    }
    ArrayNode opts = optionDefsArray(v, f);
    if (!opts.isEmpty()) {
      root.set("rentOptionDefinitions", opts);
    }
    return root;
  }

  private static String categoryLabel(Vehicle v) {
    return Optional.ofNullable(v.getStatusDefinition().getLabelTr())
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .orElse("Müsait");
  }

  private static String nz(String s) {
    return s == null ? "" : s;
  }


  private ArrayNode specsArray(Vehicle v, JsonNodeFactory f) {
    ArrayNode a = f.arrayNode();
    a.add("Otomatik");
    int seats = v.getSeats() != null && v.getSeats() > 0 ? v.getSeats() : 5;
    a.add(seats + " kişi");
    int year = v.getYear() != null ? v.getYear() : 2024;
    a.add(String.valueOf(year));
    return a;
  }

  private static String feTransmission(String code) {
    if (code == null || code.isBlank()) {
      return "otomatik";
    }
    String c = code.trim().toLowerCase(Locale.ROOT);
    if (c.contains("manuel") || c.contains("manual")) {
      return "manuel";
    }
    return "otomatik";
  }

  private static String feFuel(String code) {
    if (code == null || code.isBlank()) {
      return "benzin";
    }
    String c = code.trim().toLowerCase(Locale.ROOT);
    if (c.contains("dizel") || c.contains("diesel")) {
      return "dizel";
    }
    if (c.contains("hibrit") || c.contains("hybrid")) {
      return "hibrit";
    }
    if (c.contains("elektrik") || c.contains("electric") || c.equals("ev")) {
      return "elektrik";
    }
    return "benzin";
  }

  private String pickPrimaryImage(Vehicle v) {
    for (VehicleImage img : v.getImages()) {
      String resolved = objectStorageService.resolvePublicUrl(img.getImageUrl());
      if (resolved != null && !resolved.isBlank()) {
        return resolved;
      }
    }
    return FALLBACK_IMAGE;
  }

  private static ArrayNode stringArray(List<String> lines, JsonNodeFactory f) {
    ArrayNode a = f.arrayNode();
    for (String line : lines) {
      if (line != null && !line.isBlank()) {
        a.add(line.trim());
      }
    }
    return a;
  }

  private static ObjectNode handoverBooking(JsonNodeFactory f, HandoverLocation loc) {
    if (loc == null || loc.getId() == null) {
      return null;
    }
    ObjectNode n = f.objectNode();
    n.put("id", String.valueOf(loc.getId()));
    n.put("name", loc.getName() != null ? loc.getName() : String.valueOf(loc.getId()));
    var ref = HandoverLocationMapper.toRef(loc);
    if (ref.surchargeEur() != null && ref.surchargeEur().compareTo(BigDecimal.ZERO) > 0) {
      n.put("surchargeEur", ref.surchargeEur().doubleValue());
    }
    return n;
  }

  private ArrayNode returnBookingArray(Vehicle v, JsonNodeFactory f) {
    ArrayNode arr = f.arrayNode();
    v.getAllowedReturnHandovers().stream()
        .sorted(
            Comparator.comparingInt(VehicleAllowedReturnHandover::getLineOrder)
                .thenComparing(
                    VehicleAllowedReturnHandover::getId, Comparator.nullsLast(Comparator.naturalOrder())))
        .map(VehicleAllowedReturnHandover::getHandoverLocation)
        .forEach(
            loc -> {
              ObjectNode o = handoverBooking(f, loc);
              if (o != null) {
                arr.add(o);
              }
            });
    return arr;
  }

  private static HandoverLocation firstReturnLocation(Vehicle v) {
    if (v.getAllowedReturnHandovers().isEmpty()) {
      return null;
    }
    return v.getAllowedReturnHandovers().stream()
        .min(
            Comparator.comparingInt(VehicleAllowedReturnHandover::getLineOrder)
                .thenComparing(
                    VehicleAllowedReturnHandover::getId, Comparator.nullsLast(Comparator.naturalOrder())))
        .map(VehicleAllowedReturnHandover::getHandoverLocation)
        .orElse(null);
  }

  private ArrayNode optionDefsArray(Vehicle v, JsonNodeFactory f) {
    ArrayNode arr = f.arrayNode();
    for (VehicleOptionDefinition d : v.getOptionDefinitions()) {
      if (d.getId() == null) {
        continue;
      }
      ObjectNode o = f.objectNode();
      o.put("id", String.valueOf(d.getId()));
      o.put("title", d.getTitle() != null ? d.getTitle() : "");
      if (d.getDescription() != null && !d.getDescription().isBlank()) {
        o.put("description", d.getDescription().trim());
      }
      o.put("price", d.getPrice() != null ? d.getPrice().doubleValue() : 0);
      o.put("active", d.isActive());
      arr.add(o);
    }
    return arr;
  }
}
