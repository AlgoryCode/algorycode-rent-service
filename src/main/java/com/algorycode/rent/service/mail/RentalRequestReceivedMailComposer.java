package com.algorycode.rent.service.mail;

import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.messaging.QueuedMailMessage;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Thymeleaf şablonlarından “talebiniz alındı” e-postası üretir, Rabbit için {@link QueuedMailMessage} döner. */
@Service
public class RentalRequestReceivedMailComposer {

  public static final String TEMPLATE_CODE = "rent.request.received";

  private static final Locale TR = Locale.forLanguageTag("tr-TR");
  private static final DateTimeFormatter DATE_TR =
      DateTimeFormatter.ofPattern("d MMMM yyyy", TR);

  private final ThymeleafMailRenderer thymeleafMailRenderer;

  public RentalRequestReceivedMailComposer(ThymeleafMailRenderer thymeleafMailRenderer) {
    this.thymeleafMailRenderer = thymeleafMailRenderer;
  }

  public QueuedMailMessage compose(RentalRequest request) {
    String mailSubject = "Rezervasyon talebiniz alınmıştır";
    Context ctx = new Context(TR);
    ctx.setVariable("mailSubject", mailSubject);
    ctx.setVariable("customerName", safe(request.getCustomer().getFullName()));
    ctx.setVariable("referenceNo", safe(request.getReferenceNo()));
    ctx.setVariable("startDateFormatted", request.getStartDate().format(DATE_TR));
    ctx.setVariable("endDateFormatted", request.getEndDate().format(DATE_TR));
    ctx.setVariable("vehicleLine", vehicleLine(request));

    String html = thymeleafMailRenderer.process("mail/rental-request-received", ctx);
    String plain = thymeleafMailRenderer.process("mail/rental-request-received-plain", ctx);

    return QueuedMailMessage.multipart(
        request.getCustomer().getEmail().trim(),
        mailSubject,
        plain,
        html,
        TEMPLATE_CODE);
  }

  private static String safe(String s) {
    return s == null ? "" : s.trim();
  }

  private static String vehicleLine(RentalRequest request) {
    Vehicle v = request.getVehicle();
    if (v == null) {
      return "Belirtilmedi";
    }
    String plate = safe(v.getPlate());
    String brand = safe(v.getBrand());
    String model = safe(v.getModel());
    String core = (brand + " " + model).trim();
    if (core.isEmpty() && plate.isEmpty()) {
      return "—";
    }
    if (plate.isEmpty()) {
      return core;
    }
    if (core.isEmpty()) {
      return plate;
    }
    return core + " (" + plate + ")";
  }
}
