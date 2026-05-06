package com.algorycode.rent.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleBrand;
import com.algorycode.rent.domain.vehicle.VehicleImage;
import com.algorycode.rent.domain.vehicle.VehicleImageSlot;
import com.algorycode.rent.domain.vehicle.VehicleModel;
import com.algorycode.rent.domain.vehicle.VehicleStatus;
import com.algorycode.rent.domain.vehicle.VehicleStatusDefinition;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class VehicleMapperTest {

  @Test
  void toDto_mapsImagesBySlotName() {
    var v = new Vehicle();
    v.setId(1L);
    v.setPlate("34 X 1");
    VehicleBrand brand = new VehicleBrand();
    brand.setName("B");
    VehicleModel model = new VehicleModel();
    model.setBrand(brand);
    model.setName("M");
    v.setVehicleModel(model);
    VehicleStatusDefinition sd = new VehicleStatusDefinition();
    sd.setCode("maintenance");
    v.setStatusDefinition(sd);
    v.setYear(2024);
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
    assertThat(dto.status()).isEqualTo(VehicleStatus.maintenance);
    assertThat(dto.statusCode()).isEqualTo("maintenance");
    assertThat(dto.defaultPickupHandoverLocation()).isNull();
    assertThat(dto.defaultReturnHandoverLocation()).isNull();
    assertThat(dto.returnHandoverLocations()).isEmpty();
    assertThat(dto.optionDefinitions()).isEmpty();
    assertThat(dto.highlights()).isEmpty();
    assertThat(dto.feFleetSnapshot()).isNull();
  }

  @Test
  void toDto_whenVehicleModelNull_mapsModelIdBrandModelNullOrEmpty() {
    var v = new Vehicle();
    v.setId(2L);
    v.setPlate(null);
    VehicleStatusDefinition sd = new VehicleStatusDefinition();
    sd.setCode("available");
    v.setStatusDefinition(sd);
    v.setYear(2023);
    v.setCreatedAt(Instant.now());
    v.setUpdatedAt(Instant.now());

    var dto = VehicleMapper.toDto(v);

    assertThat(dto.vehicleModelId()).isNull();
    assertThat(dto.brand()).isEmpty();
    assertThat(dto.model()).isEmpty();
  }
}
