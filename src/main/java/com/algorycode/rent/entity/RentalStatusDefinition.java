package com.algorycode.rent.entity;

import com.algorycode.rent.entity.AbstractVehicleCatalogRow;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "rental_statuses")
public class RentalStatusDefinition extends AbstractVehicleCatalogRow {}
