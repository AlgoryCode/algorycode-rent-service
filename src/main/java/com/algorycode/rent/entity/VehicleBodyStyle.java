package com.algorycode.rent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicle_body_styles")
public class VehicleBodyStyle extends AbstractVehicleCatalogRow {}
