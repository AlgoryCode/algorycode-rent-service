package com.algorycode.rent.service;

import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

  @Mock private VehicleRepository vehicleRepository;

  @InjectMocks private VehicleService vehicleService;

  @Test
  void listAll_mapsAllVehicles() {
    var v = sampleVehicle();
    when(vehicleRepository.findAll()).thenReturn(List.of(v));

    var result = vehicleService.listAll();

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().plate()).isEqualTo("34 ABC 101");
    assertThat(result.getFirst().brand()).isEqualTo("Toyota");
  }

  @Test
  void getById_returnsDtoWhenFound() {
    var id = UUID.randomUUID();
    var v = sampleVehicle();
    v.setId(id);
    when(vehicleRepository.findById(id)).thenReturn(Optional.of(v));

    var dto = vehicleService.getById(id);

    assertThat(dto.id()).isEqualTo(id);
  }

  @Test
  void getById_throwsWhenMissing() {
    var id = UUID.randomUUID();
    when(vehicleRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> vehicleService.getById(id))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Vehicle not found");
  }

  private static Vehicle sampleVehicle() {
    var v = new Vehicle();
    v.setId(UUID.randomUUID());
    v.setPlate("34 ABC 101");
    v.setBrand("Toyota");
    v.setModel("Corolla");
    v.setYear(2023);
    v.setMaintenance(false);
    v.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
    v.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
    return v;
  }
}
