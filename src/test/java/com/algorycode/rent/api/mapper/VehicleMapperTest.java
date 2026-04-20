package com.algorycode.rent.api.mapper;

import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleImage;
import com.algorycode.rent.domain.vehicle.VehicleImageSlot;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleMapperTest {

  @Test
  void toDto_mapsImagesBySlotName() {
    var v = new Vehicle();
    v.setId(1L);
    v.setPlate("34 X 1");
    v.setBrand("B");
    v.setModel("M");
    v.setYear(2024);
    v.setMaintenance(true);
    v.setCreatedAt(Instant.now());
    v.setUpdatedAt(Instant.now());

    var img = new VehicleImage();
    img.setId(1L);
    img.setSlot(VehicleImageSlot.front);
    img.setImageUrl("https://example.com/a.jpg");
    img.setVehicle(v);
    img.setCreatedAt(Instant.now());
    img.setUpdatedAt(Instant.now());
    v.getImages().add(img);

    var dto = VehicleMapper.toDto(v);

    assertThat(dto.images()).containsEntry("front", "https://example.com/a.jpg");
    assertThat(dto.maintenance()).isTrue();
    assertThat(dto.defaultPickupHandoverLocation()).isNull();
    assertThat(dto.defaultReturnHandoverLocation()).isNull();
    assertThat(dto.returnHandoverLocations()).isEmpty();
    assertThat(dto.optionDefinitions()).isEmpty();
    assertThat(dto.highlights()).isEmpty();
  }
}
