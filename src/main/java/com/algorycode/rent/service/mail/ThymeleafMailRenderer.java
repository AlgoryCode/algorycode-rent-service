package com.algorycode.rent.service.mail;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/** Thymeleaf ile sınıf yolundaki {@code templates/} şablonlarını işler. */
@Service
public class ThymeleafMailRenderer {

  private final SpringTemplateEngine templateEngine;

  public ThymeleafMailRenderer(SpringTemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
  }

  public String process(String templateName, Context context) {
    return templateEngine.process(templateName, context);
  }
}
