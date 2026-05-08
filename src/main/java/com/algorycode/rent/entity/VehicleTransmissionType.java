package com.algorycode.rent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicle_transmission_types")
public class VehicleTransmissionType extends AbstractVehicleCatalogRow {}
