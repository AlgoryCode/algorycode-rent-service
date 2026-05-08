package com.algorycode.rent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicle_statuses")
public class VehicleStatusCatalog extends AbstractVehicleCatalogRow {}
