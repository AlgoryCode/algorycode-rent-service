package com.algorycode.rent.contract;

import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.config.AppContractProperties;
import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.domain.request.RentalRequestAdditionalDriver;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
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

  private static final float PAGE_MARGIN = 24f;
  private static final float DOC_IMAGE_MAX_HEIGHT = 130f;
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  private final AppContractProperties contractProperties;

  public RentalContractPdfService(AppContractProperties contractProperties) {
    this.contractProperties = contractProperties;
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

    try (PDDocument output = createBaseDocument()) {
      PDPage page = output.getPage(0);
      PDRectangle media = page.getMediaBox();

      try (PDPageContentStream cs =
          new PDPageContentStream(output, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
        float docAreaTop = PAGE_MARGIN + DOC_IMAGE_MAX_HEIGHT + 12f;
        writeSummary(cs, request, docAreaTop, media);
        drawIdentityImages(cs, output, request, media);
      }

      output.save(outputFile.toFile());
      return outputFile.toAbsolutePath().normalize().toString();
    } catch (IOException ex) {
      throw new BadRequestException("Sözleşme PDF üretilemedi: " + ex.getMessage());
    }
  }

  private PDDocument createBaseDocument() throws IOException {
    Path template = resolveTemplatePath();
    if (template != null && Files.isRegularFile(template)) {
      try (PDDocument source = PDDocument.load(template.toFile())) {
        PDDocument out = new PDDocument();
        if (source.getNumberOfPages() == 0) {
          out.addPage(new PDPage(PDRectangle.A4));
          return out;
        }
        out.importPage(source.getPage(0));
        return out;
      }
    }
    PDDocument out = new PDDocument();
    out.addPage(new PDPage(PDRectangle.A4));
    return out;
  }

  private void writeSummary(PDPageContentStream cs, RentalRequest req, float y, PDRectangle media)
      throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("Reference: " + req.getReferenceNo() + " | Status: " + req.getStatus().name());
    lines.add("Lessee: " + req.getCustomer().getFullName() + " | Phone: " + req.getCustomer().getPhone());
    lines.add(
        "Date Range: "
            + req.getStartDate().format(DATE_FMT)
            + " - "
            + req.getEndDate().format(DATE_FMT));
    lines.add("Passport: " + req.getCustomer().getPassportNo() + " | License: " + req.getCustomer().getDriverLicenseNo());
    lines.add(
        "Outside Country: "
            + (req.isOutsideCountryTravel() ? "YES" : "NO")
            + " | Green Insurance Fee: "
            + req.getGreenInsuranceFee());
    if (req.getVehicle() != null) {
      lines.add(
          "Vehicle: "
              + req.getVehicle().getPlate()
              + " "
              + req.getVehicle().getBrand()
              + " "
              + req.getVehicle().getModel());
    }
    if (req.getNote() != null && !req.getNote().isBlank()) {
      lines.add("Note: " + req.getNote());
    }

    int i = 1;
    for (RentalRequestAdditionalDriver d : req.getAdditionalDrivers()) {
      if (i > 3) {
        lines.add("Additional Drivers: +" + (req.getAdditionalDrivers().size() - 3) + " more");
        break;
      }
      lines.add(
          "Additional Driver "
              + i
              + ": "
              + d.getFullName()
              + " ("
              + d.getBirthDate().format(DATE_FMT)
              + "), Passport "
              + d.getPassportNo()
              + ", License "
              + d.getDriverLicenseNo());
      i++;
    }

    float startY = Math.min(y + 100f, media.getHeight() - PAGE_MARGIN - 14f);
    cs.beginText();
    cs.setFont(PDType1Font.HELVETICA, 9f);
    cs.setLeading(11f);
    cs.newLineAtOffset(PAGE_MARGIN, startY);
    for (String line : lines) {
      cs.showText(safeText(line, 140));
      cs.newLine();
    }
    cs.endText();
  }

  private void drawIdentityImages(PDPageContentStream cs, PDDocument output, RentalRequest req, PDRectangle media)
      throws IOException {
    float areaWidth = media.getWidth() - 2 * PAGE_MARGIN;
    float singleMaxWidth = (areaWidth - PAGE_MARGIN) / 2f;
    float leftX = PAGE_MARGIN;
    float rightX = PAGE_MARGIN + singleMaxWidth + PAGE_MARGIN;
    float y = PAGE_MARGIN;

    PDImageXObject license = imageFromDataUrl(output, req.getCustomer().getDriverLicenseImageDataUrl(), "license");
    PDImageXObject passport = imageFromDataUrl(output, req.getCustomer().getPassportImageDataUrl(), "passport");

    drawImageOrPlaceholder(cs, license, leftX, y, singleMaxWidth, DOC_IMAGE_MAX_HEIGHT, "Driver License");
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
    cs.setFont(PDType1Font.HELVETICA_BOLD, 9f);
    cs.newLineAtOffset(x, y + maxHeight + 2f);
    cs.showText(title);
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
    cs.addRect(x, y, maxWidth, maxHeight);
    cs.stroke();
  }

  private static PDImageXObject imageFromDataUrl(PDDocument doc, String dataUrl, String name) throws IOException {
    if (dataUrl == null || dataUrl.isBlank()) {
      return null;
    }
    String raw = dataUrl.trim();
    int idx = raw.indexOf("base64,");
    if (idx >= 0) {
      raw = raw.substring(idx + "base64,".length());
    }
    byte[] bytes = Base64.getDecoder().decode(raw);
    return PDImageXObject.createFromByteArray(doc, bytes, name);
  }

  private Path resolveTemplatePath() {
    String v = contractProperties.templatePath();
    if (v == null || v.isBlank()) {
      return null;
    }
    return Paths.get(v.trim());
  }

  private Path resolveOutputDir() {
    String v = contractProperties.outputDir();
    if (v == null || v.isBlank()) {
      return Paths.get("generated-contracts");
    }
    return Paths.get(v.trim());
  }

  private static String safeText(String raw, int maxChars) {
    if (raw == null) {
      return "";
    }
    String txt =
        Normalizer.normalize(raw, Normalizer.Form.NFKD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^\\x20-\\x7E]", " ")
            .replaceAll("[\\r\\n]+", " ")
            .trim();
    if (txt.length() <= maxChars) {
      return txt;
    }
    return txt.substring(0, maxChars - 3) + "...";
  }
}
