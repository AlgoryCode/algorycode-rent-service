package com.algorycode.rent.domain.request;

/** Kiralama talebi faturalandırma satırı türü (DB string). */
public enum RentalRequestPricedLineType {
  /** Gün × günlük kira (TRY). */
  BASE_RENTAL,
  /** Alış/teslim noktası farkı + güzergâh; tutar TRY, EUR detay metadata. */
  HANDOVER_SURCHARGE,
  /** Yurt dışı çıkış (yeşil sigorta) — {@link RentalRequest#getGreenInsuranceFee()} ile uyumlu. */
  ABROAD_USAGE,
  /** Rezervasyon ek şablonu. */
  RESERVATION_EXTRA,
  /** Araç opsiyon tanımı. */
  VEHICLE_OPTION,
  /** Serbest satır (şablonsuz). */
  CUSTOM_LINE
}
