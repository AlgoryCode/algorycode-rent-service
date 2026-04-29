package com.algorycode.rent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.algorycode.rent.api.dto.VehicleLookupCreateRequest;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.domain.vehicle.VehicleStatusDefinition;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.repository.VehicleStatusDefinitionRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleStatusDefinitionServiceTest {

  @Mock private VehicleStatusDefinitionRepository vehicleStatusDefinitionRepository;
  @Mock private VehicleRepository vehicleRepository;

  @InjectMocks private VehicleStatusDefinitionService vehicleStatusDefinitionService;

  @Test
  void delete_whenUsedByVehicle_thenThrowsConflict() {
    VehicleStatusDefinition row = new VehicleStatusDefinition();
    row.setId(9L);
    row.setCode("custom");
    row.setLabelTr("Özel");
    row.setSortOrder(5);

    when(vehicleStatusDefinitionRepository.findById(9L)).thenReturn(Optional.of(row));
    when(vehicleRepository.countByStatusDefinition_IdAndDeletedFalse(9L)).thenReturn(3L);

    assertThatThrownBy(() -> vehicleStatusDefinitionService.delete(9L))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("3");

    verify(vehicleStatusDefinitionRepository, never()).delete(any());
  }

  @Test
  void delete_whenUnused_thenDeletes() {
    VehicleStatusDefinition row = new VehicleStatusDefinition();
    row.setId(2L);
    row.setCode("orphan");
    row.setLabelTr("Yetim");
    row.setSortOrder(99);

    when(vehicleStatusDefinitionRepository.findById(2L)).thenReturn(Optional.of(row));
    when(vehicleRepository.countByStatusDefinition_IdAndDeletedFalse(2L)).thenReturn(0L);

    vehicleStatusDefinitionService.delete(2L);

    verify(vehicleStatusDefinitionRepository).delete(row);
  }

  @Test
  void create_withExplicitCode_thenSaves() {
    when(vehicleStatusDefinitionRepository.findByCodeIgnoreCase("fleet_hold")).thenReturn(Optional.empty());
    when(vehicleStatusDefinitionRepository.save(any(VehicleStatusDefinition.class)))
        .thenAnswer(
            inv -> {
              VehicleStatusDefinition e = inv.getArgument(0);
              e.setId(100L);
              return e;
            });

    var dto =
        vehicleStatusDefinitionService.create(
            new VehicleLookupCreateRequest("fleet_hold", "Filoda beklemede", 12));

    assertThat(dto.code()).isEqualTo("fleet_hold");
    assertThat(dto.labelTr()).isEqualTo("Filoda beklemede");
    assertThat(dto.sortOrder()).isEqualTo(12);
    assertThat(dto.id()).isEqualTo(100L);
  }
}
