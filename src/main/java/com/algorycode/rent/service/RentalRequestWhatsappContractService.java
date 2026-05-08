package com.algorycode.rent.service;

import com.algorycode.rent.config.AppWhatsappProperties;
import com.algorycode.rent.entity.RentalRequest;
import com.algorycode.rent.repository.RentalRequestRepository;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class RentalRequestWhatsappContractService {

  private static final Logger log =
      LoggerFactory.getLogger(RentalRequestWhatsappContractService.class);

  private final AppWhatsappProperties props;
  private final RentalRequestRepository rentalRequestRepository;
  private final ObjectStorageService objectStorageService;

  /**
   * Sözleşme PDF'ini yönetici numarasına iletmek için: önce {@code webhookUrl} (JSON + base64 PDF),
   * yoksa CallMeBot ile kısa metin bildirimi. Webhook ile n8n / Evolution API / Twilio vb.
   * bağlanabilir.
   */
  public void notifyAdminWithContractPdf(RentalRequest request) {
    if (!props.enabled()) {
      return;
    }
    String phoneDigits = digitsOnly(props.adminPhoneE164());
    if (phoneDigits.isEmpty()) {
      markError(request, "WhatsApp: admin telefonu yapılandırılmadı");
      return;
    }
    String pdfPathStr = request.getContractPdfPath();
    if (pdfPathStr == null || pdfPathStr.isBlank()) {
      markError(request, "WhatsApp: PDF yolu yok");
      return;
    }
    byte[] pdfBytes;
    try {
      Path pdfPath = Path.of(pdfPathStr);
      if (Files.isRegularFile(pdfPath)) {
        pdfBytes = Files.readAllBytes(pdfPath);
      } else {
        pdfBytes = objectStorageService.readObjectBytes(pdfPathStr);
      }
    } catch (IOException e) {
      markError(request, truncate("PDF okunamadı: " + e.getMessage(), 500));
      return;
    } catch (Exception e) {
      markError(request, truncate("PDF okunamadı: " + e.getMessage(), 500));
      return;
    }

    String caption =
        "Yeni kiralama talebi "
            + request.getReferenceNo()
            + " — "
            + request.getCustomer().getFullName()
            + " | PDF ekte (webhook) veya panelden indirin.";

    try {
      if (!props.webhookUrl().isEmpty()) {
        postWebhook(phoneDigits, request, pdfBytes, caption);
        markSent(request);
        return;
      }
      if (!props.callmebotApiKey().isEmpty()) {
        sendCallMeBotText(phoneDigits, shortenForCallMeBot(request));
        markSent(request);
        log.warn(
            "WhatsApp: yalnızca CallMeBot metin bildirimi gönderildi; PDF için app.whatsapp.webhook-url "
                + "tanımlayın (ör. n8n + Evolution API). Referans: {}",
            request.getReferenceNo());
        return;
      }
      markError(
          request, "WhatsApp: webhook-url veya callmebot-api-key tanımlı değil; PDF gönderilemedi");
    } catch (RestClientException | IllegalArgumentException e) {
      log.warn("WhatsApp bildirimi başarısız: {}", e.toString());
      markError(request, truncate(e.getMessage(), 500));
    }
  }

  private void postWebhook(
      String phoneDigits, RentalRequest request, byte[] pdfBytes, String caption) {
    String b64 = Base64.getEncoder().encodeToString(pdfBytes);
    String filename = safeFileName(request.getReferenceNo()) + ".pdf";

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("toPhoneE164", phoneDigits);
    body.put("referenceNo", request.getReferenceNo());
    body.put("customerName", request.getCustomer().getFullName());
    body.put("filename", filename);
    body.put("caption", caption);
    body.put("documentBase64", b64);

    RestClient.Builder builder =
        RestClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    if (!props.webhookAuthBearer().isEmpty()) {
      builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.webhookAuthBearer());
    }
    builder
        .build()
        .post()
        .uri(URI.create(props.webhookUrl()))
        .body(body)
        .retrieve()
        .toBodilessEntity();
  }

  private void sendCallMeBotText(String phoneDigits, String text) {
    String phoneParam = phoneDigits.startsWith("+") ? phoneDigits : "+" + phoneDigits;
    String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
    String encodedKey = URLEncoder.encode(props.callmebotApiKey(), StandardCharsets.UTF_8);
    String url =
        "https://api.callmebot.com/whatsapp.php?phone="
            + URLEncoder.encode(phoneParam, StandardCharsets.UTF_8)
            + "&text="
            + encodedText
            + "&source=algory-rent&apikey="
            + encodedKey;
    RestClient.create().get().uri(url).retrieve().toBodilessEntity();
  }

  private void markSent(RentalRequest request) {
    request.setWhatsappContractSentAt(Instant.now());
    request.setWhatsappContractError(null);
    rentalRequestRepository.save(request);
  }

  private void markError(RentalRequest request, String message) {
    request.setWhatsappContractSentAt(null);
    request.setWhatsappContractError(message);
    rentalRequestRepository.save(request);
  }

  private static String shortenForCallMeBot(RentalRequest request) {
    String t =
        "Yeni talep "
            + request.getReferenceNo()
            + " | "
            + request.getCustomer().getFullName()
            + " | PDF: yönetim paneli Talepler.";
    if (t.length() > 900) {
      return t.substring(0, 897) + "...";
    }
    return t;
  }

  private static String digitsOnly(String raw) {
    if (raw == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      if (c >= '0' && c <= '9') {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static String safeFileName(String ref) {
    return ref.replaceAll("[^A-Za-z0-9_-]", "_");
  }

  private static String truncate(String s, int max) {
    if (s == null) {
      return "";
    }
    if (s.length() <= max) {
      return s;
    }
    return s.substring(0, max - 3) + "...";
  }
}
