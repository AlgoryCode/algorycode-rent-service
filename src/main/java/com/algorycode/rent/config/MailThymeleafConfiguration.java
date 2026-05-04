package com.algorycode.rent.config;

import java.nio.charset.StandardCharsets;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 * Düz metin e-postaları için {@code *.txt} (TEXT modu) çözücü. HTML, Boot varsayılanı ({@code
 * spring.thymeleaf.prefix} + {@code .html}).
 */
@Configuration
public class MailThymeleafConfiguration {

  @Bean
  ITemplateResolver mailPlainTextTemplateResolver(
      ApplicationContext applicationContext, ThymeleafProperties properties) {
    SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
    resolver.setApplicationContext(applicationContext);
    resolver.setPrefix(properties.getPrefix());
    resolver.setSuffix(".txt");
    resolver.setTemplateMode(TemplateMode.TEXT);
    resolver.setCharacterEncoding(
        properties.getEncoding() != null
            ? properties.getEncoding().name()
            : StandardCharsets.UTF_8.name());
    resolver.setCacheable(properties.isCache());
    resolver.setCheckExistence(true);
    /* Varsayılan .html çözücüsünden sonra; aynı mantıksal ad için yalnızca .txt dosyası vardır. */
    resolver.setOrder(Ordered.LOWEST_PRECEDENCE);
    return resolver;
  }
}
