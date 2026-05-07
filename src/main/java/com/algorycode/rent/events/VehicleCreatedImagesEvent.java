package com.algorycode.rent.events;

import java.util.Map;

public record VehicleCreatedImagesEvent(Long vehicleId, Map<String, String> images) {}
