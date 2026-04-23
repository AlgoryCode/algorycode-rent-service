package com.algorycode.rent.service.readmodel;

import com.algorycode.rent.domain.location.HandoverLocation;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** user-fe `HeroHandoverOption` / `feHandoverSnapshot` satırı ile uyumlu vitrin JSON’u. */
public final class FeHandoverSnapshotJson {

  private FeHandoverSnapshotJson() {}

  public static ObjectNode forRow(HandoverLocation e) {
    ObjectNode n = JsonNodeFactory.instance.objectNode();
    if (e.getId() != null) {
      n.put("id", String.valueOf(e.getId()));
    }
    n.put("label", e.getName() != null ? e.getName() : "");
    String cc = e.getCountryCode();
    if (cc != null && !cc.isBlank()) {
      n.put("countryCode", cc.trim());
    } else {
      n.putNull("countryCode");
    }
    n.put("lineOrder", e.getLineOrder());
    return n;
  }
}
