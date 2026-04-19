package com.algorycode.rent.logging;

/** İş kuralı / audit log reason kodları — KVKK için serbest metin yerine sabit kod. */
public final class SafeReasonCodes {

  private SafeReasonCodes() {}

  public static final String RENTAL_OVERLAP = "rental_overlap";
  public static final String RESOURCE_NOT_FOUND = "resource_not_found";
  public static final String CONFLICT = "conflict";
  public static final String BAD_REQUEST = "bad_request";
  public static final String UNHANDLED_EXCEPTION = "unhandled_exception";
}
