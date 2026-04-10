package com.algorycode.rent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.object-storage")
public record AppObjectStorageProperties(
    boolean enabled,
    String endpoint,
    String bucket,
    String accessKey,
    String secretKey,
    String keyPrefix,
    Integer presignExpirySeconds) {

  public AppObjectStorageProperties {
    if (endpoint == null || endpoint.isBlank()) endpoint = "https://s3.algorycode.com";
    if (bucket == null || bucket.isBlank()) bucket = "rent";
    if (accessKey == null || accessKey.isBlank()) accessKey = "ld88HanEcv4q2BR0";
    if (secretKey == null || secretKey.isBlank()) secretKey = "DuE4w8K3jONhT8FfTd2ZaUzkszlJ4r0i";
    if (keyPrefix == null || keyPrefix.isBlank()) keyPrefix = "prod";
    if (presignExpirySeconds == null || presignExpirySeconds <= 0) presignExpirySeconds = 3600;
  }
}
