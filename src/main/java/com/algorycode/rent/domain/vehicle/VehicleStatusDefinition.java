package com.algorycode.rent.domain.vehicle;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicle_statuses")
public class VehicleStatusDefinition extends AbstractVehicleCatalogRow {}
