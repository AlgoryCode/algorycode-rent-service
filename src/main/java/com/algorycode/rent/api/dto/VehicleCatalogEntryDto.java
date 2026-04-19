package com.algorycode.rent.api.dto;

/** Araç referans kataloğu satırı (gövde tipi, yakıt, vites). */
public record VehicleCatalogEntryDto(String code, String labelTr, int sortOrder) {}
