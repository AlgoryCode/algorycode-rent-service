package com.algorycode.rent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.algorycode.rent.entity.RentalStatus;
import com.algorycode.rent.entity.Vehicle;
import com.algorycode.rent.entity.VehicleStatus;
import com.algorycode.rent.entity.VehicleStatusCatalog;
import com.algorycode.rent.repository.VehicleRepository;
import com.algorycode.rent.repository.VehicleStatusCatalogRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleCatalogStatusServiceTest {

  @Mock private VehicleRepository vehicleRepository;
  @Mock private VehicleStatusCatalogRepository vehicleStatusCatalogRepository;

  @InjectMocks private VehicleCatalogStatusService vehicleCatalogStatusService;

  @Test
  void updateVehicleCatalogStatus_setsCatalogAndSaves() {
    Vehicle v = new Vehicle();
    v.setId(5L);
    v.setVehicleStatus(VehicleStatus.ACTIVE);
    when(vehicleRepository.findByIdAndDeletedFalse(5L)).thenReturn(Optional.of(v));
    VehicleStatusCatalog rented = new VehicleStatusCatalog();
    rented.setId(99L);
    rented.setCode("rented");
    when(vehicleStatusCatalogRepository.findByCodeIgnoreCase("rented"))
        .thenReturn(Optional.of(rented));

    vehicleCatalogStatusService.updateVehicleCatalogStatus(5L, "rented");

    ArgumentCaptor<Vehicle> cap = ArgumentCaptor.forClass(Vehicle.class);
    verify(vehicleRepository).save(cap.capture());
    assertThat(cap.getValue().getStatus()).isEqualTo(VehicleStatus.RENTED);
  }

  @Test
  void catalogCodeForRentalStatus_active_isRented() {
    assertThat(vehicleCatalogStatusService.catalogCodeForRentalStatus(RentalStatus.ACTIVE))
        .isEqualTo("rented");
  }

  @Test
  void catalogCodeForRentalStatus_completed_isAvailable() {
    assertThat(vehicleCatalogStatusService.catalogCodeForRentalStatus(RentalStatus.COMPLETED))
        .isEqualTo("available");
  }

  @Test
  void updateVehicleFromRentalStatus_active_setsRented() {
    Vehicle v = new Vehicle();
    v.setId(3L);
    v.setVehicleStatus(VehicleStatus.ACTIVE);
    when(vehicleRepository.findByIdAndDeletedFalse(3L)).thenReturn(Optional.of(v));

    vehicleCatalogStatusService.updateVehicleFromRentalStatus(3L, RentalStatus.ACTIVE);

    assertThat(v.getStatus()).isEqualTo(VehicleStatus.RENTED);
    verify(vehicleRepository).save(v);
  }
}
