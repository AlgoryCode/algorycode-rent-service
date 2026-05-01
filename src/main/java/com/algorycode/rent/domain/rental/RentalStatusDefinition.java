package com.algorycode.rent.domain.rental;

import com.algorycode.rent.domain.vehicle.AbstractVehicleCatalogRow;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "rental_statuses")
public class RentalStatusDefinition extends AbstractVehicleCatalogRow {}
