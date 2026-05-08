package com.algorycode.rent.service.mail;

import com.algorycode.rent.entity.RentalRequest;
import com.algorycode.rent.entity.RentalRequestStatus;
import com.algorycode.rent.messaging.QueuedMailMessage;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

/** Talep durumu değişince Thymeleaf ile e-posta üretir. */
@Service
@RequiredArgsConstructor
public class RentalRequestStatusMailComposer {

  public static final String TEMPLATE_CODE = "rent.request.status.updated";

  private final ThymeleafMailRenderer thymeleafMailRenderer;

  public QueuedMailMessage compose(RentalRequest request) {
    String mailSubject = "Rezervasyon talep durumunuz güncellendi";
    String name =
        request.getCustomer().getFullName() != null
            ? request.getCustomer().getFullName().trim()
            : "";
    String statusMessage =
        request.getStatusMessage() != null ? request.getStatusMessage().trim() : "";
    boolean hasStatusMessage = !statusMessage.isEmpty();

    Context ctx = new Context(Locale.forLanguageTag("tr-TR"));
    ctx.setVariable("mailSubject", mailSubject);
    ctx.setVariable("customerName", name.isEmpty() ? "Müşteri" : name);
    ctx.setVariable("referenceNo", request.getReferenceNo());
    ctx.setVariable("statusDisplay", statusDisplayTr(request.getStatus()));
    ctx.setVariable("hasStatusMessage", hasStatusMessage);
    ctx.setVariable("statusMessage", statusMessage);

    String html = thymeleafMailRenderer.process("mail/rental-request-status", ctx);
    String plain = thymeleafMailRenderer.process("mail/rental-request-status-plain", ctx);

    return QueuedMailMessage.multipart(
        request.getCustomer().getEmail().trim(), mailSubject, plain, html, TEMPLATE_CODE);
  }

  private static String statusDisplayTr(RentalRequestStatus status) {
    if (status == null) {
      return "—";
    }
    return switch (status) {
      case pending -> "Beklemede";
      case approved -> "Onaylandı";
      case rejected -> "Reddedildi";
    };
  }
}
