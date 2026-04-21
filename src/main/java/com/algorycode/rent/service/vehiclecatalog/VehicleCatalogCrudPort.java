package com.algorycode.rent.service.vehiclecatalog;

import com.algorycode.rent.api.dto.VehicleCatalogEntryDto;
import com.algorycode.rent.api.dto.VehicleLookupCreateRequest;
import com.algorycode.rent.api.dto.VehicleLookupUpdateRequest;
import java.util.List;

/** Araç katalog (gövde / yakıt / vites) CRUD sözleşmesi — aynı iş akışını tek arayüzde toplar. */
public interface VehicleCatalogCrudPort {

  List<VehicleCatalogEntryDto> listAll();

  VehicleCatalogEntryDto getByCode(String code);

  VehicleCatalogEntryDto create(VehicleLookupCreateRequest req);

  VehicleCatalogEntryDto update(String code, VehicleLookupUpdateRequest req);

  void delete(long id);
}
