package com.algorycode.rent.domain.rental;

/** FE kiralama statüleri. */
public enum RentalStatus {
  active,
  pending,
  completed,
  cancelled;

  public static RentalStatus fromCode(String code) {
    if (code == null || code.isBlank()) {
      return active;
    }
    return valueOf(code.trim().toLowerCase());
  }
}
