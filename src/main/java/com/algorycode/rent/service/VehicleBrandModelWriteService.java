package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.VehicleBrandCatalogDto;
import com.algorycode.rent.api.dto.VehicleModelCatalogDto;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.vehicle.VehicleBrand;
import com.algorycode.rent.domain.vehicle.VehicleModel;
import com.algorycode.rent.repository.VehicleBrandRepository;
import com.algorycode.rent.repository.VehicleModelRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleBrandModelWriteService {

  private final VehicleBrandRepository vehicleBrandRepository;
  private final VehicleModelRepository vehicleModelRepository;

  @Transactional
  public VehicleBrandCatalogDto createBrand(String name, int sortOrder) {
    VehicleBrand b = new VehicleBrand();
    b.setName(name.trim());
    b.setSortOrder(sortOrder);
    b = vehicleBrandRepository.save(b);
    return new VehicleBrandCatalogDto(b.getId(), b.getName(), b.getSortOrder(), List.of());
  }

  @Transactional
  public VehicleModelCatalogDto createModel(long brandId, String name, int sortOrder) {
    VehicleBrand brand =
        vehicleBrandRepository
            .findById(brandId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle brand not found: " + brandId));
    VehicleModel m = new VehicleModel();
    m.setBrand(brand);
    m.setName(name.trim());
    m.setSortOrder(sortOrder);
    m = vehicleModelRepository.save(m);
    return new VehicleModelCatalogDto(m.getId(), m.getName(), m.getSortOrder());
  }
}
