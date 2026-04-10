package com.algorycode.rent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.contract")
public record AppContractProperties(
    /** İsteğe bağlı harici PDF şablonu; boşsa sözleşme tamamen kod ile çizilir. */
    String templatePath,
    String outputDir,
    /** İngilizce “Authorization for rental cars” üst paragrafı ve dipnot için şirket bilgileri. */
    AuthorizationLetter authorization) {

  public AppContractProperties {
    if (authorization == null) {
      authorization = AuthorizationLetter.defaults();
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
