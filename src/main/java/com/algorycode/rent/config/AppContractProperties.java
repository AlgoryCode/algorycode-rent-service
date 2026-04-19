package com.algorycode.rent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app.contract")
public record AppContractProperties(
    /** İsteğe bağlı harici PDF şablonu; boşsa sözleşme tamamen kod ile çizilir. */
    String templatePath,
    String outputDir,
    /**
     * PDF satir kalemlerinde KDV dagilimi: opsiyon / sigorta / lokasyon tutarlari KDV dahil (brut) kabul edilir.
     * Ornek 20: net = brut / 1.20. 0: vergi sutunu 0, net = brut.
     */
    BigDecimal pdfLineVatPercent,
    /** İngilizce “Authorization for rental cars” üst paragrafı ve dipnot için şirket bilgileri. */
    AuthorizationLetter authorization) {

  public AppContractProperties {
    if (authorization == null) {
      authorization = AuthorizationLetter.defaults();
    }
    if (pdfLineVatPercent == null) {
      pdfLineVatPercent = new BigDecimal("20");
    }
  }

  public record AuthorizationLetter(
      String taxablePersonName,
      String niptNumber,
      String representativeName,
      String representativeBirthPlace,
      String representativeResidence,
      String representativeBirthDate,
      String representativePassportNo,
      String footerCompany,
      String footerAddress,
      String roadsideAssistancePhone) {

    static AuthorizationLetter defaults() {
      return new AuthorizationLetter("", "", "", "", "", "", "", "", "", "+90 510 220 553");
    }
  }
}
