package com.algorycode.rent.contract;

import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.config.AppContractProperties;
import com.algorycode.rent.config.AppContractProperties.AuthorizationLetter;
import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.domain.request.RentalRequestCustomerSnapshot;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.service.ObjectStorageService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class RentalContractPdfService {

  private static final float PAGE_MARGIN = 40f;
  private static final float DOC_IMAGE_MAX_HEIGHT = 108f;
  private static final float GAP_ABOVE_IMAGES = 10f;
  private static final float TITLE_SIZE = 13f;
  private static final float BODY_SIZE = 9f;
  private static final float TABLE_SIZE = 8.5f;
  private static final float LEADING_BODY = 11.5f;
  private static final float LEADING_TABLE = 11f;
  private static final DateTimeFormatter DATE_SLASH = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private final AppContractProperties contractProperties;
  private final ObjectStorageService objectStorageService;

  public RentalContractPdfService(
      AppContractProperties contractProperties, ObjectStorageService objectStorageService) {
    this.contractProperties = contractProperties;
    this.objectStorageService = objectStorageService;
  }

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
      float reservedBottom =
          PAGE_MARGIN + DOC_IMAGE_MAX_HEIGHT + GAP_ABOVE_IMAGES + 22f;

      try (PDPageContentStream cs =
          new PDPageContentStream(output, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
        float y = media.getHeight() - PAGE_MARGIN;
        y = drawTitle(cs, y, media.getWidth());
        y -= 6f;
        y = drawIntroParagraph(cs, y, reservedBottom, media.getWidth());
        y -= 8f;
        y = drawInfoTable(cs, request, y, reservedBottom, media.getWidth());
        y -= 10f;
        y = drawWrappedBlock(
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

        drawIdentityImages(cs, output, request, media);
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

  private float drawInfoTable(PDPageContentStream cs, RentalRequest req, float y, float minY, float pageW)
      throws IOException {
    RentalRequestCustomerSnapshot c = req.getCustomer();
    Vehicle v = req.getVehicle();

    String brand = v != null ? nz(v.getBrand()) : "-";
    String model = v != null ? nz(v.getModel()) : "-";
    String plate = v != null ? nz(v.getPlate()) : "-";
    String color = "-";
    String fuel = "-";

    String issueDate = req.getStartDate() != null ? req.getStartDate().format(DATE_SLASH) : "-";
    String retDate = req.getEndDate() != null ? req.getEndDate().format(DATE_SLASH) : "-";

    String[][] rows = {
      {"Brand:", brand, "Name Surname:", nz(c.getFullName())},
      {"Model:", model, "ID Number:", nz(c.getNationalId(), "-")},
      {"Color:", color, "Driver's License Number:", nz(c.getDriverLicenseNo())},
      {"License Plate:", plate, "Phone:", nz(c.getPhone())},
      {"Fuel Type:", fuel, "Place of Birth:", "-"},
      {"Issue Date:", issueDate + "    Time: ______", "", ""},
      {"Return Date:", retDate + "    Time: ______", "", ""},
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

  private void drawLabelValue(PDPageContentStream cs, float x, float y, float maxW, String label, String value)
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

  private float drawPlainLine(PDPageContentStream cs, String text, float y, float pageW) throws IOException {
    cs.beginText();
    cs.setFont(PDType1Font.HELVETICA, BODY_SIZE);
    cs.newLineAtOffset(PAGE_MARGIN, y - BODY_SIZE);
    cs.showText(safeAscii(text));
    cs.endText();
    return y - LEADING_BODY;
  }

  private float drawWrappedBlock(PDPageContentStream cs, String text, float y, float minY, float pageW)
      throws IOException {
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

  private void drawIdentityImages(PDPageContentStream cs, PDDocument output, RentalRequest req, PDRectangle media)
      throws IOException {
    float areaWidth = media.getWidth() - 2 * PAGE_MARGIN;
    float singleMaxWidth = (areaWidth - 12f) / 2f;
    float leftX = PAGE_MARGIN;
    float rightX = PAGE_MARGIN + singleMaxWidth + 12f;
    float y = PAGE_MARGIN;

    PDImageXObject license =
        resolveIdentityImage(output, req.getCustomer().getDriverLicenseImageDataUrl(), "license");
    PDImageXObject passport =
        resolveIdentityImage(output, req.getCustomer().getPassportImageDataUrl(), "passport");

    drawImageOrPlaceholder(cs, license, leftX, y, singleMaxWidth, DOC_IMAGE_MAX_HEIGHT, "Driver's license");
    drawImageOrPlaceholder(cs, passport, rightX, y, singleMaxWidth, DOC_IMAGE_MAX_HEIGHT, "Passport");
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

  private PDImageXObject resolveIdentityImage(PDDocument doc, String stored, String name) throws IOException {
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

  private static PDImageXObject loadImageFromDataUrl(PDDocument doc, String dataUrl, String name) throws IOException {
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
