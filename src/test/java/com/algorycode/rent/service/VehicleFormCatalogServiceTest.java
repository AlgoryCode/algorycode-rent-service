package com.algorycode.rent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.algorycode.rent.domain.location.HandoverLocationKind;
import com.algorycode.rent.repository.VehicleBrandRepository;
import com.algorycode.rent.repository.VehicleStatusCatalogRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class VehicleFormCatalogServiceTest {

  @Mock private VehicleBrandRepository vehicleBrandRepository;
  @Mock private VehicleStatusCatalogRepository vehicleStatusCatalogRepository;
  @Mock private CountryService countryService;
  @Mock private HandoverLocationService handoverLocationService;
  @Mock private VehicleOptionTemplateService vehicleOptionTemplateService;

  @InjectMocks private VehicleFormCatalogServiceImpl vehicleFormCatalogService;

  @Test
  void load_whenRepositoriesEmpty_thenReturnsEmptyCollections() {
    when(vehicleBrandRepository.findAllWithModelsForCatalog()).thenReturn(List.of());
    when(vehicleStatusCatalogRepository.findAll(any(Sort.class))).thenReturn(List.of());
    when(countryService.listAll()).thenReturn(List.of());
    when(handoverLocationService.list(eq(HandoverLocationKind.PICKUP), eq(false)))
        .thenReturn(List.of());
    when(handoverLocationService.list(eq(HandoverLocationKind.RETURN), eq(false)))
        .thenReturn(List.of());
    when(vehicleOptionTemplateService.list(false)).thenReturn(List.of());

    var dto = vehicleFormCatalogService.load();

    assertThat(dto.brands()).isEmpty();
    assertThat(dto.vehicleStatuses()).isEmpty();
    assertThat(dto.countries()).isEmpty();
    assertThat(dto.pickupHandoverLocations()).isEmpty();
    assertThat(dto.returnHandoverLocations()).isEmpty();
    assertThat(dto.optionTemplates()).isEmpty();
  }
}
