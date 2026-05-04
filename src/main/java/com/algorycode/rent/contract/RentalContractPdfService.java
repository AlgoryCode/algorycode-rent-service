package com.algorycode.rent.contract;

import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.config.AppContractProperties;
import com.algorycode.rent.config.AppContractProperties.AuthorizationLetter;
import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.domain.request.RentalRequestAdditionalDriver;
import com.algorycode.rent.domain.request.RentalRequestCustomerSnapshot;
import com.algorycode.rent.domain.request.RentalRequestOption;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.service.ObjectStorageService;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalContractPdfService {

  private static final float PAGE_MARGIN = 40f;
  private static final float DOC_IMAGE_MAX_HEIGHT = 108f;
  private static final float TITLE_SIZE = 13f;
  private static final float BODY_SIZE = 9f;
  private static final float TABLE_SIZE = 8.5f;
  private static final float LEADING_BODY = 11.5f;
  private static final float LEADING_TABLE = 11f;
  private static final DateTimeFormatter DATE_SLASH = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter TIME_HM = DateTimeFormatter.ofPattern("HH:mm");
  private static final LocalTime DEFAULT_PICK_RETURN_TIME = LocalTime.of(8, 0);
  private static final float INV_FONT = 7.5f;
  private static final float INV_HEADER_FONT = 8f;
  private static final float INV_ROW_LEADING = 9.5f;

  private final AppContractProperties contractProperties;
  private final ObjectStorageService objectStorageService;

  public String generateFor(RentalRequest request) {
    Path outDir = resolveOutputDir();
    try {
      Files.createDirectories(outDir);
    } catch (IOException ex) {
      throw new BadRequestException("Sözleşme çıktı klasörü oluşturulamadı: " + ex.getMessage());
    }

    String safeRef = request.getReferenceNo().replaceAll("[^A-Za-z0-9_-]", "_");
    Path outputFile = outDir.resolve(safeRef + ".pdf");

    try (PDDocument output = createDocument()) {
      PDPage page = output.getPage(0);
      PDRectangle media = page.getMediaBox();
      List<RentalRequestAdditionalDriver> adDrivers =
          request.getAdditionalDrivers() != null ? request.getAdditionalDrivers() : List.of();
      int docBlockCount = 1 + adDrivers.size();
      float page1DocImgH = page1DocumentImageHeight(docBlockCount);
      float reservedBottom = reservedBottomForDocumentStack(media, docBlockCount, page1DocImgH);

      try (PDPageContentStream cs =
          new PDPageContentStream(
              output, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
        float y = media.getHeight() - PAGE_MARGIN;
        y = drawTitle(cs, y, media.getWidth());
        y -= 6f;
        y = drawIntroParagraph(cs, y, reservedBottom, media.getWidth());
        y -= 8f;
        y = drawInfoTable(cs, request, y, reservedBottom, media.getWidth());
        y -= 10f;
        y = drawLineItemsVatTable(cs, request, y, reservedBottom, media.getWidth());
        y -= 8f;
        y =
            drawWrappedBlock(
                cs,
                "The lessee is obligated to return the vehicle on the designated day and at the designated time. "
                    + "Any costs for damages that may occur due to misuse will be covered by the user. Additionally, "
                    + "they must make all necessary payments in cases of rule violations.",
                y,
                reservedBottom,
                media.getWidth());
        y -= 8f;
        y = drawSignOff(cs, y, media.getWidth());
        y -= 6f;
        y = drawFooter(cs, y, media.getWidth());

        y -= 10f;
        float hardMinY = PAGE_MARGIN;
        var cust = request.getCustomer();
        y =
            drawDriverIdSectionOnFirstPage(
                cs,
                output,
                y,
                hardMinY,
                media.getWidth(),
                "PRIMARY LESSEE / DRIVER - DOCUMENTS (" + nz(cust.getFullName()) + ")",
                cust.getDriverLicenseImageDataUrl(),
                cust.getPassportImageDataUrl(),
                "lessee-",
                page1DocImgH);
        int adIdx = 0;
        for (RentalRequestAdditionalDriver d : adDrivers) {
          adIdx++;
          y =
              drawDriverIdSectionOnFirstPage(
                  cs,
                  output,
                  y,
                  hardMinY,
                  media.getWidth(),
                  "ADDITIONAL DRIVER " + adIdx + " - DOCUMENTS (" + nz(d.getFullName()) + ")",
                  d.getDriverLicenseImageDataUrl(),
                  d.getPassportImageDataUrl(),
                  "ad" + adIdx + "-",
                  page1DocImgH);
        }
      }

      output.save(outputFile.toFile());
      return outputFile.toAbsolutePath().normalize().toString();
    } catch (IOException ex) {
      throw new BadRequestException("Sözleşme PDF üretilemedi: " + ex.getMessage());
    }
  }

  private PDDocument createDocument() throws IOException {
    PDDocument out = new PDDocument();
    out.addPage(new PDPage(PDRectangle.A4));
    return out;
  }

  /** Birinci sayfada ust metin ile cakismayi onlemek icin alt bant rezervi. */
  private static float reservedBottomForDocumentStack(
      PDRectangle media, int documentBlockCount, float imageHeight) {
    float perBlock = 36f + imageHeight + 14f;
    float want = PAGE_MARGIN + documentBlockCount * perBlock + 16f;
    float cap = PAGE_MARGIN + media.getHeight() * 0.5f;
    return Math.min(Math.max(PAGE_MARGIN + 28f, want), cap);
  }

  /** Iki blok (birincil + ek sofor) sigsin diye goruntu yuksekligi. */
  private static float page1DocumentImageHeight(int blockCount) {
    if (blockCount <= 1) {
      return Math.min(96f, DOC_IMAGE_MAX_HEIGHT);
    }
    if (blockCount == 2) {
      return 64f;
    }
    return Math.max(38f, 128f / blockCount);
  }

  /** Ehliyet + pasaport satirini 1. sayfada cizer; bir sonraki blok icin y (asagi) dondurur. */
  private float drawDriverIdSectionOnFirstPage(
      PDPageContentStream cs,
      PDDocument doc,
      float yStart,
      float hardMinY,
      float pageW,
      String heading,
      String licenseDataUrl,
      String passportDataUrl,
      String imageNamePrefix,
      float maxImageHeight)
      throws IOException {
    float headFont = TITLE_SIZE - 2f;
    float maxTextW = pageW - 2 * PAGE_MARGIN;
    float y = yStart;
    if (y < hardMinY + maxImageHeight + 32f) {
      return yStart;
    }
    for (String line : wrapWords(heading, PDType1Font.HELVETICA_BOLD, headFont, maxTextW)) {
      if (y - headFont < hardMinY + maxImageHeight + 6f) {
        return y;
      }
      cs.beginText();
      cs.setFont(PDType1Font.HELVETICA_BOLD, headFont);
      cs.newLineAtOffset(PAGE_MARGIN, y - headFont);
      cs.showText(safeAscii(line));
      cs.endText();
      y -= LEADING_BODY + 1f;
    }
    y -= 6f;
    float imgBottom = y - maxImageHeight;
    if (imgBottom < hardMinY) {
      imgBottom = hardMinY;
    }
    float areaWidth = pageW - 2 * PAGE_MARGIN;
    float singleMaxWidth = (areaWidth - 12f) / 2f;
    float leftX = PAGE_MARGIN;
    float rightX = PAGE_MARGIN + singleMaxWidth + 12f;
    PDImageXObject license = resolveIdentityImage(doc, licenseDataUrl, imageNamePrefix + "lic");
    PDImageXObject passport = resolveIdentityImage(doc, passportDataUrl, imageNamePrefix + "ppt");
    drawImageOrPlaceholder(
        cs, license, leftX, imgBottom, singleMaxWidth, maxImageHeight, "Driver's license");
    drawImageOrPlaceholder(
        cs, passport, rightX, imgBottom, singleMaxWidth, maxImageHeight, "Passport");
    return imgBottom - 8f;
  }

  private float drawTitle(PDPageContentStream cs, float y, float pageW) throws IOException {
    String title = "AUTHORIZATION FOR RENTAL CARS";
    float w = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000f * TITLE_SIZE;
    float x = (pageW - w) / 2f;
    cs.beginText();
    cs.setFont(PDType1Font.HELVETICA_BOLD, TITLE_SIZE);
    cs.newLineAtOffset(x, y - TITLE_SIZE);
    cs.showText(title);
    cs.endText();
    return y - TITLE_SIZE - 4f;
  }

  private float drawIntroParagraph(PDPageContentStream cs, float y, float minY, float pageWidth)
      throws IOException {
    AuthorizationLetter a = contractProperties.authorization();
    String intro =
        "Taxable Person "
            + nz(a.taxablePersonName())
            + " with NIPT number "
            + nz(a.niptNumber())
            + ", represented by "
            + nz(a.representativeName())
            + ", born in "
            + nz(a.representativeBirthPlace())
            + " and residing in "
            + nz(a.representativeResidence())
            + ", born on (date) "
            + nz(a.representativeBirthDate())
            + " identified with PASSPORT number "
            + nz(a.representativePassportNo())
            + ", authorizes the use of the vehicle within and outside the territory of Albania.";
    return drawWrappedBlock(cs, intro, y, minY, pageWidth);
  }

  private float drawInfoTable(
      PDPageContentStream cs, RentalRequest req, float y, float minY, float pageW)
      throws IOException {
    RentalRequestCustomerSnapshot c = req.getCustomer();
    Vehicle v = req.getVehicle();

    String brand = v != null ? nz(v.getBrand()) : "-";
    String model = v != null ? nz(v.getModel()) : "-";
    String plate = v != null ? nz(v.getPlate()) : "-";
    String color = v != null ? nz(v.getBodyColor(), "-") : "-";
    String fuel = resolveFuelTypeDisplay(v);

    String birth = c.getBirthDate() != null ? c.getBirthDate().format(DATE_SLASH) : "-";

    String issueDate = req.getStartDate() != null ? req.getStartDate().format(DATE_SLASH) : "-";
    String retDate = req.getEndDate() != null ? req.getEndDate().format(DATE_SLASH) : "-";
    LocalTime st = req.getStartTime() != null ? req.getStartTime() : DEFAULT_PICK_RETURN_TIME;
    LocalTime rt = req.getReturnTime() != null ? req.getReturnTime() : DEFAULT_PICK_RETURN_TIME;
    String startTimeStr = st.format(TIME_HM);
    String returnTimeStr = rt.format(TIME_HM);

    String[][] rows = {
      {"Brand:", brand, "Name Surname:", nz(c.getFullName())},
      {"Model:", model, "ID Number:", nz(c.getNationalId(), "-")},
      {"Color:", color, "Driver's License Number:", nz(c.getDriverLicenseNo())},
      {"License Plate:", plate, "Phone:", nz(c.getPhone())},
      {"Fuel Type:", fuel, "Date of Birth:", birth},
      {"Issue Date:", issueDate + "    Time: " + startTimeStr, "", ""},
      {"Return Date:", retDate + "    Time: " + returnTimeStr, "", ""},
    };

    float x0 = PAGE_MARGIN;
    float tableW = pageW - 2 * PAGE_MARGIN;
    float mid = x0 + tableW / 2f;
    float colW = tableW / 2f - 4f;
    float rowH = LEADING_TABLE + 2f;

    float yCursor = y;
    for (String[] row : rows) {
      if (yCursor < minY + rowH) {
        break;
      }
      float lineBase = yCursor - TABLE_SIZE;
      drawLabelValue(cs, x0 + 2f, lineBase, colW, row[0], row[1]);
      if (!row[2].isEmpty()) {
        drawLabelValue(cs, mid + 2f, lineBase, colW, row[2], row[3]);
      }
      cs.setLineWidth(0.35f);
      cs.moveTo(x0, yCursor - rowH + 2f);
      cs.lineTo(x0 + tableW, yCursor - rowH + 2f);
      cs.stroke();
      yCursor -= rowH;
    }
    cs.setLineWidth(0.5f);
    cs.addRect(x0, yCursor, tableW, y - yCursor + 2f);
    cs.stroke();
    cs.moveTo(mid, y);
    cs.lineTo(mid, yCursor);
    cs.stroke();

    return yCursor - 4f;
  }

  private static String resolveFuelTypeDisplay(Vehicle v) {
    if (v == null) {
      return "-";
    }
    String ft = nz(v.getFuelType());
    if (!ft.isEmpty()) {
      return ft;
    }
    String eng = nz(v.getEngine());
    return eng.isEmpty() ? "-" : eng;
  }

  private record PdfInvoiceRow(String service, BigDecimal grossInclVat, boolean priced) {}

  private List<PdfInvoiceRow> buildInvoiceRows(RentalRequest req) {
    List<PdfInvoiceRow> rows = new ArrayList<>();
    List<RentalRequestOption> opts = req.getOptions() != null ? req.getOptions() : List.of();
    opts.stream()
        .sorted(Comparator.comparingInt(RentalRequestOption::getLineOrder))
        .forEach(
            o -> {
              String svc = nz(o.getTitle(), "(service)");
              String d = nz(o.getDescription());
              if (!d.isEmpty()) {
                svc = svc + " — " + d.replace('\n', ' ');
              }
              rows.add(new PdfInvoiceRow(svc, o.getPrice(), true));
            });
    if (req.isOutsideCountryTravel()) {
      rows.add(new PdfInvoiceRow("Cross-border / travel outside Albania (requested)", null, false));
    }
    if (positiveAmount(req.getGreenInsuranceFee())) {
      rows.add(
          new PdfInvoiceRow("Green card / border insurance", req.getGreenInsuranceFee(), true));
    }
    if (positiveAmount(req.getHandoverTotalEur())) {
      rows.add(
          new PdfInvoiceRow(
              "Pickup / return location surcharges", req.getHandoverTotalEur(), true));
    }
    List<RentalRequestAdditionalDriver> drivers =
        req.getAdditionalDrivers() != null ? req.getAdditionalDrivers() : List.of();
    int idx = 0;
    for (RentalRequestAdditionalDriver d : drivers) {
      idx++;
      String dob = d.getBirthDate() != null ? d.getBirthDate().format(DATE_SLASH) : "-";
      String svc =
          "Additional driver #"
              + idx
              + ": "
              + nz(d.getFullName())
              + "; DOB "
              + dob
              + "; DL "
              + nz(d.getDriverLicenseNo(), "-")
              + "; Passport "
              + nz(d.getPassportNo(), "-");
      rows.add(new PdfInvoiceRow(svc, null, false));
    }
    return rows;
  }

  private record VatParts(BigDecimal net, BigDecimal vat, BigDecimal gross) {}

  private VatParts splitVatInclusive(BigDecimal grossInclVat) {
    if (grossInclVat == null || grossInclVat.compareTo(BigDecimal.ZERO) <= 0) {
      return new VatParts(
          BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
          BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
          BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }
    BigDecimal rate = contractProperties.pdfLineVatPercent();
    if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
      BigDecimal g = grossInclVat.setScale(2, RoundingMode.HALF_UP);
      return new VatParts(g, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), g);
    }
    BigDecimal divisor =
        BigDecimal.ONE.add(rate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
    BigDecimal net = grossInclVat.divide(divisor, 2, RoundingMode.HALF_UP);
    BigDecimal vat = grossInclVat.subtract(net).setScale(2, RoundingMode.HALF_UP);
    BigDecimal gross = grossInclVat.setScale(2, RoundingMode.HALF_UP);
    return new VatParts(net, vat, gross);
  }

  /** Ek hizmet ve ucretler: hizmet adi, net, KDV, brut (EUR). */
  private float drawLineItemsVatTable(
      PDPageContentStream cs, RentalRequest req, float y, float minY, float pageW)
      throws IOException {
    List<PdfInvoiceRow> rows = buildInvoiceRows(req);
    if (rows.isEmpty()) {
      return y;
    }
    float x0 = PAGE_MARGIN;
    float tableW = pageW - 2 * PAGE_MARGIN;
    float c0 = x0;
    float c1 = x0 + tableW * 0.52f;
    float c2 = x0 + tableW * 0.69f;
    float c3 = x0 + tableW * 0.84f;
    float wNum = tableW * 0.16f;

    if (y - BODY_SIZE < minY) {
      return y;
    }
    cs.beginText();
    cs.setFont(PDType1Font.HELVETICA_BOLD, BODY_SIZE);
    cs.newLineAtOffset(x0, y - BODY_SIZE);
    cs.showText(safeAscii("ADDITIONAL SERVICES AND CHARGES (NET / VAT / GROSS, EUR)"));
    cs.endText();
    y -= BODY_SIZE + 6f;

    float hdrBaseline = y - INV_HEADER_FONT;
    cs.beginText();
    cs.setFont(PDType1Font.HELVETICA_BOLD, INV_HEADER_FONT);
    cs.newLineAtOffset(c0 + 2f, hdrBaseline);
    cs.showText(safeAscii("Service / description"));
    cs.endText();
    drawRightText(cs, "Net", c1 + wNum - 2f, hdrBaseline, INV_HEADER_FONT, true);
    drawRightText(cs, "VAT", c2 + wNum - 2f, hdrBaseline, INV_HEADER_FONT, true);
    drawRightText(cs, "Gross", c3 + wNum - 2f, hdrBaseline, INV_HEADER_FONT, true);
    float underHeader = hdrBaseline - 5f;
    cs.setLineWidth(0.35f);
    cs.moveTo(x0, underHeader);
    cs.lineTo(x0 + tableW, underHeader);
    cs.stroke();
    y = underHeader - 6f;

    BigDecimal sumNet = BigDecimal.ZERO;
    BigDecimal sumVat = BigDecimal.ZERO;
    BigDecimal sumGross = BigDecimal.ZERO;

    for (PdfInvoiceRow row : rows) {
      List<String> svcLines =
          wrapWords(row.service(), PDType1Font.HELVETICA, INV_FONT, c1 - c0 - 4f);
      if (svcLines.isEmpty()) {
        svcLines = new ArrayList<>();
        svcLines.add(" ");
      }
      float rowH = svcLines.size() * INV_ROW_LEADING + 8f;
      if (y - rowH < minY) {
        break;
      }
      float rowTop = y;
      float lineBaseline = rowTop - 4f - INV_FONT;
      for (String sl : svcLines) {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, INV_FONT);
        cs.newLineAtOffset(c0 + 2f, lineBaseline);
        cs.showText(safeAscii(sl));
        cs.endText();
        lineBaseline -= INV_ROW_LEADING;
      }
      float valBaseline = rowTop - INV_FONT - 4f;
      if (row.priced() && row.grossInclVat() != null) {
        VatParts p = splitVatInclusive(row.grossInclVat());
        sumNet = sumNet.add(p.net());
        sumVat = sumVat.add(p.vat());
        sumGross = sumGross.add(p.gross());
        drawRightText(cs, formatMoneyPlain(p.net()), c1 + wNum - 2f, valBaseline, INV_FONT, false);
        drawRightText(cs, formatMoneyPlain(p.vat()), c2 + wNum - 2f, valBaseline, INV_FONT, false);
        drawRightText(
            cs, formatMoneyPlain(p.gross()), c3 + wNum - 2f, valBaseline, INV_FONT, false);
      } else {
        drawRightText(cs, "-", c1 + wNum - 2f, valBaseline, INV_FONT, false);
        drawRightText(cs, "-", c2 + wNum - 2f, valBaseline, INV_FONT, false);
        drawRightText(cs, "-", c3 + wNum - 2f, valBaseline, INV_FONT, false);
      }
      y = rowTop - rowH;
      cs.setLineWidth(0.2f);
      cs.moveTo(x0, y + 2f);
      cs.lineTo(x0 + tableW, y + 2f);
      cs.stroke();
    }

    y -= 6f;
    if (y < minY + 24f) {
      return y;
    }
    cs.setLineWidth(0.45f);
    cs.moveTo(x0, y);
    cs.lineTo(x0 + tableW, y);
    cs.stroke();
    y -= 6f;
    float tb = y - INV_FONT;
    cs.beginText();
    cs.setFont(PDType1Font.HELVETICA_BOLD, INV_FONT);
    cs.newLineAtOffset(c0 + 2f, tb);
    cs.showText(safeAscii("TOTAL"));
    cs.endText();
    drawRightText(cs, formatMoneyPlain(sumNet), c1 + wNum - 2f, tb, INV_FONT, true);
    drawRightText(cs, formatMoneyPlain(sumVat), c2 + wNum - 2f, tb, INV_FONT, true);
    drawRightText(cs, formatMoneyPlain(sumGross), c3 + wNum - 2f, tb, INV_FONT, true);
    return tb - INV_ROW_LEADING - 8f;
  }

  private void drawRightText(
      PDPageContentStream cs,
      String text,
      float rightX,
      float baselineY,
      float fontSize,
      boolean bold)
      throws IOException {
    String s = safeAscii(text);
    PDFont font = bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA;
    float w = font.getStringWidth(s) / 1000f * fontSize;
    cs.beginText();
    cs.setFont(font, fontSize);
    cs.newLineAtOffset(rightX - w, baselineY);
    cs.showText(s);
    cs.endText();
  }

  private static String formatMoneyPlain(BigDecimal amount) {
    return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  private static boolean positiveAmount(BigDecimal amount) {
    return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
  }

  private void drawLabelValue(
      PDPageContentStream cs, float x, float y, float maxW, String label, String value)
      throws IOException {
    String lab = safeAscii(label);
    String val = safeAscii(value);
    cs.beginText();
    cs.setFont(PDType1Font.HELVETICA_BOLD, TABLE_SIZE);
    cs.newLineAtOffset(x, y);
    cs.showText(lab);
    cs.endText();
    float lw = PDType1Font.HELVETICA_BOLD.getStringWidth(lab) / 1000f * TABLE_SIZE;
    float vx = x + lw + 2f;
    float rem = maxW - lw - 4f;
    String vtrunc = truncateToWidth(val, PDType1Font.HELVETICA, TABLE_SIZE, rem);
    cs.beginText();
    cs.setFont(PDType1Font.HELVETICA, TABLE_SIZE);
    cs.newLineAtOffset(vx, y);
    cs.showText(vtrunc);
    cs.endText();
  }

  private float drawSignOff(PDPageContentStream cs, float y, float pageW) throws IOException {
    AuthorizationLetter a = contractProperties.authorization();
    String road = nzDefault(a.roadsideAssistancePhone(), "+90 510 220 553");

    y = drawPlainLine(cs, "Lessee: _______________________________", y, pageW);
    y -= 4f;
    y = drawPlainLine(cs, "Payment: _______________________________", y, pageW);
    y -= 8f;
    cs.beginText();
    cs.setFont(PDType1Font.HELVETICA_BOLD, BODY_SIZE);
    String line = "Roadside assistance " + road;
    float w = PDType1Font.HELVETICA_BOLD.getStringWidth(safeAscii(line)) / 1000f * BODY_SIZE;
    cs.newLineAtOffset((pageW - w) / 2f, y - BODY_SIZE);
    cs.showText(safeAscii(line));
    cs.endText();
    return y - BODY_SIZE - 6f;
  }

  private float drawFooter(PDPageContentStream cs, float y, float pageW) throws IOException {
    AuthorizationLetter a = contractProperties.authorization();
    String company = nzDefault(a.footerCompany(), nz(a.taxablePersonName()));
    String addr = nz(a.footerAddress());

    if (!company.isEmpty()) {
      cs.beginText();
      cs.setFont(PDType1Font.HELVETICA_BOLD, BODY_SIZE);
      String sc = safeAscii(company);
      float w = PDType1Font.HELVETICA_BOLD.getStringWidth(sc) / 1000f * BODY_SIZE;
      cs.newLineAtOffset((pageW - w) / 2f, y - BODY_SIZE);
      cs.showText(sc);
      cs.endText();
      y -= BODY_SIZE + 4f;
    }
    if (!addr.isEmpty()) {
      y = drawWrappedCentered(cs, "Address: " + addr, y, pageW);
    }
    return y;
  }

  private float drawWrappedCentered(PDPageContentStream cs, String text, float y, float pageW)
      throws IOException {
    float maxW = pageW - 2 * PAGE_MARGIN;
    List<String> lines = wrapWords(text, PDType1Font.HELVETICA_OBLIQUE, BODY_SIZE - 0.5f, maxW);
    for (String line : lines) {
      String s = safeAscii(line);
      float w = PDType1Font.HELVETICA_OBLIQUE.getStringWidth(s) / 1000f * (BODY_SIZE - 0.5f);
      cs.beginText();
      cs.setFont(PDType1Font.HELVETICA_OBLIQUE, BODY_SIZE - 0.5f);
      cs.newLineAtOffset((pageW - w) / 2f, y - (BODY_SIZE - 0.5f));
      cs.showText(s);
      cs.endText();
      y -= LEADING_BODY;
    }
    return y;
  }

  private float drawPlainLine(PDPageContentStream cs, String text, float y, float pageW)
      throws IOException {
    cs.beginText();
    cs.setFont(PDType1Font.HELVETICA, BODY_SIZE);
    cs.newLineAtOffset(PAGE_MARGIN, y - BODY_SIZE);
    cs.showText(safeAscii(text));
    cs.endText();
    return y - LEADING_BODY;
  }

  private float drawWrappedBlock(
      PDPageContentStream cs, String text, float y, float minY, float pageW) throws IOException {
    float maxW = pageW - 2 * PAGE_MARGIN;
    List<String> lines = wrapWords(text, PDType1Font.HELVETICA, BODY_SIZE, maxW);
    for (String line : lines) {
      if (y - BODY_SIZE < minY) {
        break;
      }
      cs.beginText();
      cs.setFont(PDType1Font.HELVETICA, BODY_SIZE);
      cs.newLineAtOffset(PAGE_MARGIN, y - BODY_SIZE);
      cs.showText(safeAscii(line));
      cs.endText();
      y -= LEADING_BODY;
    }
    return y;
  }

  private static List<String> wrapWords(String text, PDFont font, float fontSize, float maxW)
      throws IOException {
    List<String> out = new ArrayList<>();
    String[] words = text.split("\\s+");
    StringBuilder cur = new StringBuilder();
    for (String w : words) {
      String trial = cur.isEmpty() ? w : cur + " " + w;
      float tw = font.getStringWidth(safeAscii(trial)) / 1000f * fontSize;
      if (tw <= maxW) {
        cur.setLength(0);
        cur.append(trial);
      } else {
        if (!cur.isEmpty()) {
          out.add(cur.toString());
        }
        if (font.getStringWidth(safeAscii(w)) / 1000f * fontSize > maxW) {
          out.addAll(splitLongWord(w, font, fontSize, maxW));
          cur.setLength(0);
        } else {
          cur.setLength(0);
          cur.append(w);
        }
      }
    }
    if (!cur.isEmpty()) {
      out.add(cur.toString());
    }
    return out;
  }

  private static List<String> splitLongWord(String w, PDFont font, float fontSize, float maxW)
      throws IOException {
    List<String> parts = new ArrayList<>();
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < w.length(); i++) {
      char ch = w.charAt(i);
      String next = b.toString() + ch;
      if (font.getStringWidth(safeAscii(next)) / 1000f * fontSize > maxW && !b.isEmpty()) {
        parts.add(b.toString());
        b.setLength(0);
      }
      b.append(ch);
    }
    if (!b.isEmpty()) {
      parts.add(b.toString());
    }
    return parts;
  }

  private static String truncateToWidth(String text, PDFont font, float fontSize, float maxW)
      throws IOException {
    String s = safeAscii(text);
    if (font.getStringWidth(s) / 1000f * fontSize <= maxW) {
      return s;
    }
    for (int n = s.length(); n > 1; n--) {
      String t = s.substring(0, n - 1) + "...";
      if (font.getStringWidth(t) / 1000f * fontSize <= maxW) {
        return t;
      }
    }
    return ".";
  }

  private static void drawImageOrPlaceholder(
      PDPageContentStream cs,
      PDImageXObject image,
      float x,
      float y,
      float maxWidth,
      float maxHeight,
      String title)
      throws IOException {
    cs.beginText();
    cs.setFont(PDType1Font.HELVETICA_BOLD, 8f);
    cs.newLineAtOffset(x, y + maxHeight + 3f);
    cs.showText(safeAscii(title));
    cs.endText();

    if (image == null) {
      cs.addRect(x, y, maxWidth, maxHeight);
      cs.stroke();
      return;
    }
    float imgW = image.getWidth();
    float imgH = image.getHeight();
    float scale = Math.min(maxWidth / imgW, maxHeight / imgH);
    float drawW = imgW * scale;
    float drawH = imgH * scale;
    float dx = x + (maxWidth - drawW) / 2f;
    float dy = y + (maxHeight - drawH) / 2f;
    cs.drawImage(image, dx, dy, drawW, drawH);
    cs.setLineWidth(0.3f);
    cs.addRect(x, y, maxWidth, maxHeight);
    cs.stroke();
  }

  private PDImageXObject resolveIdentityImage(PDDocument doc, String stored, String name)
      throws IOException {
    if (stored == null || stored.isBlank()) {
      return null;
    }
    String v = stored.trim();
    if (v.startsWith("http://") || v.startsWith("https://")) {
      return null;
    }
    if (v.startsWith("data:")) {
      return loadImageFromDataUrl(doc, v, name);
    }
    if (objectStorageService.isActiveStorage()) {
      try {
        byte[] bytes = objectStorageService.readObjectBytes(v);
        return PDImageXObject.createFromByteArray(doc, bytes, name);
      } catch (BadRequestException ignored) {
        return null;
      }
    }
    return null;
  }

  private static PDImageXObject loadImageFromDataUrl(PDDocument doc, String dataUrl, String name)
      throws IOException {
    String raw = dataUrl.trim();
    int idx = raw.indexOf("base64,");
    if (idx >= 0) {
      raw = raw.substring(idx + "base64,".length());
    }
    raw = raw.replaceAll("\\s+", "");
    if (raw.isEmpty()) {
      return null;
    }
    byte[] bytes = decodeBase64ImagePayload(raw);
    return PDImageXObject.createFromByteArray(doc, bytes, name);
  }

  private static byte[] decodeBase64ImagePayload(String base64) {
    try {
      return Base64.getDecoder().decode(base64);
    } catch (IllegalArgumentException ignored) {
      String rfc = base64.replace('-', '+').replace('_', '/');
      return Base64.getDecoder().decode(padBase64(rfc));
    }
  }

  private static String padBase64(String s) {
    int r = s.length() % 4;
    if (r == 0) {
      return s;
    }
    return s + "=".repeat(4 - r);
  }

  private Path resolveOutputDir() {
    String v = contractProperties.outputDir();
    if (v == null || v.isBlank()) {
      return Paths.get("generated-contracts");
    }
    return Paths.get(v.trim());
  }

  private static String nz(String s) {
    return s == null ? "" : s.trim();
  }

  private static String nz(String s, String fallback) {
    String t = nz(s);
    return t.isEmpty() ? fallback : t;
  }

  private static String nzDefault(String s, String fallback) {
    String t = nz(s);
    return t.isEmpty() ? fallback : t;
  }

  private static String safeAscii(String raw) {
    if (raw == null) {
      return "";
    }
    return Normalizer.normalize(raw, Normalizer.Form.NFKD)
        .replaceAll("\\p{M}", "")
        .replaceAll("[^\\x20-\\x7E]", " ")
        .replaceAll("[\\r\\n]+", " ")
        .trim();
  }
}
