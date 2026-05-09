package com.algorycode.rent.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.algorycode.rent.entity.Vehicle;
import com.algorycode.rent.entity.VehicleBrand;
import com.algorycode.rent.entity.VehicleImage;
import com.algorycode.rent.entity.VehicleImageSlot;
import com.algorycode.rent.entity.VehicleModel;
import com.algorycode.rent.entity.VehicleStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class VehicleMapperTest {

  @Test
  void toDto_mapsImagesBySlotName() {
    var v = new Vehicle();
    v.setId(1L);
    v.setPlate("34 X 1");
    VehicleBrand brand = new VehicleBrand();
    brand.setId(11L);
    brand.setName("B");
    VehicleModel model = new VehicleModel();
    model.setId(12L);
    model.setBrandId(brand.getId());
    model.setBrand(brand);
    model.setName("M");
    v.setVehicleModelId(model.getId());
    v.setVehicleModel(model);
    v.setVehicleStatus(VehicleStatus.MAINTENANCE);
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
    assertThat(dto.status()).isEqualTo(VehicleStatus.MAINTENANCE);
    assertThat(dto.statusCode()).isEqualTo("MAINTENANCE");
    assertThat(dto.defaultPickupHandoverLocation()).isNull();
    assertThat(dto.defaultReturnHandoverLocation()).isNull();
    assertThat(dto.returnHandoverLocations()).isEmpty();
    assertThat(dto.optionDefinitions()).isEmpty();
    assertThat(dto.highlights()).isEmpty();
  }

  @Test
  void toDto_whenVehicleModelNull_mapsModelIdBrandModelNullOrEmpty() {
    var v = new Vehicle();
    v.setId(2L);
    v.setPlate(null);
    v.setVehicleStatus(VehicleStatus.ACTIVE);
    v.setYear(2023);
    v.setCreatedAt(Instant.now());
    v.setUpdatedAt(Instant.now());

    var dto = VehicleMapper.toDto(v);

    assertThat(dto.vehicleModelId()).isNull();
    assertThat(dto.brand()).isEmpty();
    assertThat(dto.model()).isEmpty();
  }
}
