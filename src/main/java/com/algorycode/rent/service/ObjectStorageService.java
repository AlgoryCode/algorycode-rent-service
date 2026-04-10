package com.algorycode.rent.service;

import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.config.AppObjectStorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ObjectStorageService {
  private static final Logger log = LoggerFactory.getLogger(ObjectStorageService.class);

  private static final Pattern DATA_URL_PATTERN =
      Pattern.compile("^data:([\\w!#$&^.+\\-]+/[\\w!#$&^.+\\-]+);base64,(.+)$", Pattern.DOTALL);
  private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private final AppObjectStorageProperties props;
  private final MinioClient minioClient;

  public ObjectStorageService(AppObjectStorageProperties props) {
    this.props = props;
    if (!props.endpoint().isBlank() && !props.accessKey().isBlank() && !props.secretKey().isBlank()) {
      this.minioClient =
          MinioClient.builder()
              .endpoint(props.endpoint())
              .credentials(props.accessKey(), props.secretKey())
              .build();
    } else {
      this.minioClient = null;
    }
  }

  public String uploadDataUrl(String folderPrefix, String baseName, String rawValue) {
    if (rawValue == null || rawValue.isBlank()) {
      return null;
    }
    String trimmed = rawValue.trim();
    if (!trimmed.startsWith("data:")) {
      return normalizeExistingReference(trimmed);
    }
    if (!isActiveStorage()) {
      return trimmed;
    }
    ParsedDataUrl parsed = parseDataUrl(trimmed);
    String extension = extensionForMimeType(parsed.mimeType());
    String key = buildObjectKey(folderPrefix, baseName, extension);
    try {
      putObject(key, parsed.bytes(), parsed.mimeType());
      return key;
    } catch (BadRequestException ex) {
      log.warn(
          "Object storage upload skipped, fallback to inline data. key={}, endpoint={}, reason={}",
          key,
          props.endpoint(),
          ex.getMessage());
      return trimmed;
    }
  }

  public String uploadBytes(String folderPrefix, String baseName, String extension, String contentType, byte[] bytes) {
    if (!isActiveStorage()) {
      throw new BadRequestException("Object storage yapılandırılamadı.");
    }
    String ext = normalizeExtension(extension);
    String key = buildObjectKey(folderPrefix, baseName, ext);
    putObject(key, bytes, contentType);
    return key;
  }

  /**
   * Veritabanında saklanan referans için bucket’taki nesneyi silmeyi dener (data URL veya presigned URL
   * ise atlar). Hata durumunda log yazar, uygulama akışını bloklamaz.
   */
  public void deleteObjectIfStored(String storedReference) {
    if (!isActiveStorage() || storedReference == null || storedReference.isBlank()) {
      return;
    }
    String v = storedReference.trim();
    if (v.startsWith("data:")) {
      return;
    }
    String objectKey = v;
    if (v.startsWith("http://") || v.startsWith("https://")) {
      String extracted = tryExtractObjectKeyFromUrl(v);
      if (extracted == null) {
        return;
      }
      objectKey = extracted;
    }
    try {
      minioClient.removeObject(
          RemoveObjectArgs.builder().bucket(props.bucket()).object(objectKey).build());
    } catch (Exception e) {
      log.warn("Object storage delete skipped: key={}, reason={}", objectKey, e.getMessage());
    }
  }

  public byte[] readObjectBytes(String objectKey) {
    if (!isActiveStorage()) {
      throw new BadRequestException("Object storage yapılandırılamadı.");
    }
    try (InputStream in = minioClient.getObject(GetObjectArgs.builder().bucket(props.bucket()).object(objectKey).build())) {
      return in.readAllBytes();
    } catch (ErrorResponseException e) {
      throw new BadRequestException("Object storage dosyası bulunamadı: " + objectKey);
    } catch (Exception e) {
      throw new BadRequestException("Object storage dosyası okunamadı: " + e.getMessage());
    }
  }

  public String resolvePublicUrl(String storedValue) {
    if (storedValue == null || storedValue.isBlank()) {
      return storedValue;
    }
    String value = storedValue.trim();
    if (value.startsWith("data:") || value.startsWith("http://") || value.startsWith("https://")) {
      return value;
    }
    if (!isActiveStorage()) {
      return value;
    }
    try {
      return minioClient.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .method(Method.GET)
              .bucket(props.bucket())
              .object(value)
              .expiry(props.presignExpirySeconds().intValue())
              .build());
    } catch (Exception e) {
      return value;
    }
  }

  private void putObject(String objectKey, byte[] bytes, String contentType) {
    try {
      ensureBucketExists();
      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(props.bucket())
              .object(objectKey)
              .stream(new ByteArrayInputStream(bytes), (long) bytes.length, -1L)
              .contentType(contentType)
              .build());
    } catch (Exception e) {
      throw new BadRequestException("Object storage yükleme hatası: " + e.getMessage());
    }
  }

  private void ensureBucketExists() throws Exception {
    boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(props.bucket()).build());
    if (!exists) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(props.bucket()).build());
    }
  }

  private boolean isConfigured() {
    if (minioClient == null) {
      return false;
    }
    return !props.endpoint().isBlank()
        && !props.bucket().isBlank()
        && !props.accessKey().isBlank()
        && !props.secretKey().isBlank();
  }

  public boolean isActiveStorage() {
    return props.enabled() && isConfigured();
  }

  private String buildObjectKey(String folderPrefix, String baseName, String extension) {
    String prefix = normalizeFolder(folderPrefix);
    String safeName = sanitize(baseName);
    String ts = FILE_TS.format(LocalDateTime.now(ZoneOffset.UTC));
    String token = UUID.randomUUID().toString().substring(0, 8);
    return prefix + "/" + safeName + "_" + ts + "_" + token + "." + extension;
  }

  private String normalizeFolder(String folderPrefix) {
    String scoped = props.keyPrefix().trim().replaceAll("^/+", "").replaceAll("/+$", "");
    String extra = folderPrefix == null ? "" : folderPrefix.trim().replaceAll("^/+", "").replaceAll("/+$", "");
    return extra.isBlank() ? scoped : scoped + "/" + extra;
  }

  private static String sanitize(String raw) {
    if (raw == null || raw.isBlank()) {
      return "file";
    }
    return raw.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]+", "_");
  }

  private static String normalizeExtension(String ext) {
    if (ext == null || ext.isBlank()) {
      return "bin";
    }
    String out = ext.trim().toLowerCase(Locale.ROOT).replace(".", "");
    return out.isBlank() ? "bin" : out;
  }

  private static ParsedDataUrl parseDataUrl(String dataUrl) {
    Matcher matcher = DATA_URL_PATTERN.matcher(dataUrl);
    if (!matcher.matches()) {
      throw new BadRequestException("Desteklenmeyen dosya formatı (data URL bekleniyor).");
    }
    String mimeType = matcher.group(1).toLowerCase(Locale.ROOT);
    String rawB64 = matcher.group(2).replaceAll("\\s+", "");
    try {
      byte[] bytes = Base64.getDecoder().decode(rawB64);
      return new ParsedDataUrl(mimeType, bytes);
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException("Base64 çözümlenemedi.");
    }
  }

  private String normalizeExistingReference(String value) {
    if (value.startsWith("http://") || value.startsWith("https://")) {
      String maybeKey = tryExtractObjectKeyFromUrl(value);
      return maybeKey != null ? maybeKey : value;
    }
    return value;
  }

  private String tryExtractObjectKeyFromUrl(String url) {
    try {
      URI input = URI.create(url);
      URI endpoint = URI.create(props.endpoint());
      if (!hostEquals(input, endpoint)) {
        return null;
      }
      String path = input.getPath();
      String bucketPrefix = "/" + props.bucket() + "/";
      if (path == null || !path.startsWith(bucketPrefix)) {
        return null;
      }
      String key = path.substring(bucketPrefix.length());
      if (key.isBlank()) {
        return null;
      }
      return URLDecoder.decode(key, StandardCharsets.UTF_8);
    } catch (Exception ignored) {
      return null;
    }
  }

  private static boolean hostEquals(URI a, URI b) {
    if (a.getHost() == null || b.getHost() == null) {
      return false;
    }
    if (!a.getHost().equalsIgnoreCase(b.getHost())) {
      return false;
    }
    int ap = a.getPort();
    int bp = b.getPort();
    if (ap == -1 && bp == -1) return true;
    return ap == bp;
  }

  private static String extensionForMimeType(String mimeType) {
    return switch (mimeType) {
      case "image/jpeg", "image/jpg" -> "jpg";
      case "image/png" -> "png";
      case "image/webp" -> "webp";
      case "application/pdf" -> "pdf";
      default -> "bin";
    };
  }

  private record ParsedDataUrl(String mimeType, byte[] bytes) {}
}
