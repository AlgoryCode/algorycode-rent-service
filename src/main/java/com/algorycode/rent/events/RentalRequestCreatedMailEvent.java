package com.algorycode.rent.events;

import java.util.UUID;

/** İşlem commit edildikten sonra müşteriye “talebiniz alındı” e-postası tetiklenir. */
public record RentalRequestCreatedMailEvent(UUID rentalRequestId) {}
