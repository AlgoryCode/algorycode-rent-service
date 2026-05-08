package com.algorycode.rent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.algorycode.rent.dto.VehicleLookupCreateRequest;
import com.algorycode.rent.exception.ConflictException;
import com.algorycode.rent.entity.VehicleStatusCatalog;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.repository.VehicleStatusCatalogRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleStatusCatalogServiceTest {

  @Mock private VehicleStatusCatalogRepository vehicleStatusCatalogRepository;
  @Mock private VehicleRepository vehicleRepository;

  @InjectMocks private VehicleStatusCatalogService vehicleStatusCatalogService;

  @Test
  void delete_whenUsedByVehicle_thenThrowsConflict() {
    VehicleStatusCatalog row = new VehicleStatusCatalog();
    row.setId(9L);
    row.setCode("custom");
    row.setLabelTr("Özel");
    row.setSortOrder(5);

    when(vehicleStatusCatalogRepository.findById(9L)).thenReturn(Optional.of(row));
    when(vehicleRepository.countByVehicleStatusIdAndDeletedFalse(9L)).thenReturn(3L);

    assertThatThrownBy(() -> vehicleStatusCatalogService.delete(9L))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("3");

    verify(vehicleStatusCatalogRepository, never()).delete(any());
  }

  @Test
  void delete_whenUnused_thenDeletes() {
    VehicleStatusCatalog row = new VehicleStatusCatalog();
    row.setId(2L);
    row.setCode("orphan");
    row.setLabelTr("Yetim");
    row.setSortOrder(99);

    when(vehicleStatusCatalogRepository.findById(2L)).thenReturn(Optional.of(row));
    when(vehicleRepository.countByVehicleStatusIdAndDeletedFalse(2L)).thenReturn(0L);

    vehicleStatusCatalogService.delete(2L);

    verify(vehicleStatusCatalogRepository).delete(row);
  }

  @Test
  void create_withExplicitCode_thenSaves() {
    when(vehicleStatusCatalogRepository.findByCodeIgnoreCase("fleet_hold"))
        .thenReturn(Optional.empty());
    when(vehicleStatusCatalogRepository.save(any(VehicleStatusCatalog.class)))
        .thenAnswer(
            inv -> {
              VehicleStatusCatalog e = inv.getArgument(0);
              e.setId(100L);
              return e;
            });

    var dto =
        vehicleStatusCatalogService.create(
            new VehicleLookupCreateRequest("fleet_hold", "Filoda beklemede", 12));

    assertThat(dto.code()).isEqualTo("fleet_hold");
    assertThat(dto.labelTr()).isEqualTo("Filoda beklemede");
    assertThat(dto.sortOrder()).isEqualTo(12);
    assertThat(dto.id()).isEqualTo(100L);
  }
}
