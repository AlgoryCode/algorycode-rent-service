package com.algorycode.rent.entity;

import com.algorycode.rent.entity.RentalRequestCustomerSnapshot;

public final class CustomerRecordKeys {

  private CustomerRecordKeys() {}

  public static String fromCustomer(Customer c) {
    if (c == null) {
      return "ph:";
    }
    String nid = c.getNationalId() == null ? "" : c.getNationalId().trim();
    if (!nid.isEmpty()) {
      return "tc:" + nid;
    }
    String phone = c.getPhone() == null ? "" : c.getPhone().trim();
    return "ph:" + phone;
  }

  public static String fromRequestCustomer(RentalRequestCustomerSnapshot c) {
    if (c == null) {
      return "ph:";
    }
    String nid = c.getNationalId() == null ? "" : c.getNationalId().trim();
    if (!nid.isEmpty()) {
      return "tc:" + nid;
    }
    String phone = c.getPhone() == null ? "" : c.getPhone().trim();
    return "ph:" + phone;
  }
}
