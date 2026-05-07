package com.algorycode.rent.events;

import com.algorycode.rent.service.VehicleImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class VehicleCreatedImagesListener {

  private final VehicleImageService vehicleImageService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCreated(VehicleCreatedImagesEvent event) {
    vehicleImageService.processVehicleImagesAndSnapshotAsync(event.vehicleId(), event.images());
  }
}
