package com.algorycode.rent.dto;

/** Araç referans kataloğu satırı (gövde tipi, yakıt, vites). */
public record VehicleCatalogEntryDto(long id, String code, String labelTr, int sortOrder) {}
