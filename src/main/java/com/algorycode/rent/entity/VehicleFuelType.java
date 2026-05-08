package com.algorycode.rent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicle_fuel_types")
public class VehicleFuelType extends AbstractVehicleCatalogRow {}
