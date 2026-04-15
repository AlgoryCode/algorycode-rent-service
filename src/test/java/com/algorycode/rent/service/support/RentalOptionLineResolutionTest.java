package com.algorycode.rent.service.support;

import com.algorycode.rent.api.dto.RentalOptionRequest;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleOptionDefinition;
import com.algorycode.rent.repository.VehicleOptionDefinitionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalOptionLineResolutionTest {

  @Mock private VehicleOptionDefinitionRepository definitionRepository;

  @Test
  void resolve_copiesFieldsFromVehicleOptionDefinition() {
    UUID vehicleId = UUID.randomUUID();
    UUID defId = UUID.randomUUID();
    Vehicle vehicle = new Vehicle();
    vehicle.setId(vehicleId);

    VehicleOptionDefinition def = new VehicleOptionDefinition();
    def.setTitle("Bebek koltuğu");
    def.setDescription("ISO fix");
    def.setPrice(new BigDecimal("12.50"));
    def.setIcon("icon-key");
    def.setActive(true);
    def.setVehicle(vehicle);

    when(definitionRepository.findByIdAndVehicle_Id(defId, vehicleId)).thenReturn(Optional.of(def));

    RentalOptionRequest req = new RentalOptionRequest(defId, null, null, null, null);
    RentalOptionLineResolution.Resolved r =
        RentalOptionLineResolution.resolve(vehicle, req, definitionRepository);

    assertThat(r.title()).isEqualTo("Bebek koltuğu");
    assertThat(r.description()).isEqualTo("ISO fix");
    assertThat(r.price()).isEqualByComparingTo("12.50");
    assertThat(r.icon()).isEqualTo("icon-key");
  }

  @Test
  void resolve_throwsWhenDefinitionInactive() {
    UUID vehicleId = UUID.randomUUID();
    UUID defId = UUID.randomUUID();
    Vehicle vehicle = new Vehicle();
    vehicle.setId(vehicleId);
    VehicleOptionDefinition def = new VehicleOptionDefinition();
    def.setTitle("X");
    def.setPrice(BigDecimal.ONE);
    def.setActive(false);
    def.setVehicle(vehicle);
    when(definitionRepository.findByIdAndVehicle_Id(defId, vehicleId)).thenReturn(Optional.of(def));

    RentalOptionRequest req = new RentalOptionRequest(defId, null, null, null, null);
    assertThatThrownBy(() -> RentalOptionLineResolution.resolve(vehicle, req, definitionRepository))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("artık kullanılamaz");
  }

  @Test
  void resolve_usesRequestBodyWhenNoDefinitionId() {
    Vehicle vehicle = new Vehicle();
    vehicle.setId(UUID.randomUUID());
    RentalOptionRequest req =
        new RentalOptionRequest(null, "Serbest satır", "Açıklama", new BigDecimal("5"), "ic");

    RentalOptionLineResolution.Resolved r =
        RentalOptionLineResolution.resolve(vehicle, req, definitionRepository);

    assertThat(r.title()).isEqualTo("Serbest satır");
    assertThat(r.description()).isEqualTo("Açıklama");
    assertThat(r.price()).isEqualByComparingTo("5");
    assertThat(r.icon()).isEqualTo("ic");
  }

  @Test
  void resolve_throwsWhenManualTitleMissing() {
    Vehicle vehicle = new Vehicle();
    vehicle.setId(UUID.randomUUID());
    RentalOptionRequest req = new RentalOptionRequest(null, "  ", null, BigDecimal.ONE, null);
    assertThatThrownBy(() -> RentalOptionLineResolution.resolve(vehicle, req, definitionRepository))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("başlığı");
  }
}
