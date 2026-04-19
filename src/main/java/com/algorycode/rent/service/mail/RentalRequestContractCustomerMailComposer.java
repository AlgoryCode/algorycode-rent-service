package com.algorycode.rent.service.mail;

import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.messaging.QueuedMailMessage;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.Locale;

/** Onaylı talep + PDF sonrası müşteriye sözleşme bildirimi (Thymeleaf → mail kuyruğu). */
@Service
public class RentalRequestContractCustomerMailComposer {

  public static final String TEMPLATE_CODE = "rent.request.contract.pdf";

  private final ThymeleafMailRenderer thymeleafMailRenderer;

  public RentalRequestContractCustomerMailComposer(ThymeleafMailRenderer thymeleafMailRenderer) {
    this.thymeleafMailRenderer = thymeleafMailRenderer;
  }

  public QueuedMailMessage compose(RentalRequest request, String contractPdfPublicUrl) {
    String mailSubject = "Kiralama sözleşmeniz hazır — " + request.getReferenceNo();
    String name =
        request.getCustomer().getFullName() != null ? request.getCustomer().getFullName().trim() : "";
    String url = contractPdfPublicUrl != null ? contractPdfPublicUrl.trim() : "";
    boolean hasPublicPdf = !url.isEmpty() && (url.startsWith("http://") || url.startsWith("https://"));

    Context ctx = new Context(Locale.forLanguageTag("tr-TR"));
    ctx.setVariable("mailSubject", mailSubject);
    ctx.setVariable("customerName", name.isEmpty() ? "Müşteri" : name);
    ctx.setVariable("referenceNo", request.getReferenceNo());
    ctx.setVariable("hasPublicPdf", hasPublicPdf);
    ctx.setVariable("contractPdfUrl", url);

    String html = thymeleafMailRenderer.process("mail/rental-request-contract", ctx);
    String plain = thymeleafMailRenderer.process("mail/rental-request-contract-plain", ctx);

    return QueuedMailMessage.multipart(
        request.getCustomer().getEmail().trim(), mailSubject, plain, html, TEMPLATE_CODE);
  }
}
