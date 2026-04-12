package com.algorycode.rent.domain.customer;

import com.algorycode.rent.domain.rental.CustomerSnapshot;
import com.algorycode.rent.domain.request.RentalRequestCustomerSnapshot;

/** FE ile aynı müşteri dizini anahtarı: önce TC, yoksa telefon. */
public final class CustomerRecordKeys {

  private CustomerRecordKeys() {}

  public static String fromRentalCustomer(CustomerSnapshot c) {
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
