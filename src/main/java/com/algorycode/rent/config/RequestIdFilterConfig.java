package com.algorycode.rent.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class RequestIdFilterConfig {

  @Bean
  public RequestIdMdcFilter requestIdMdcFilter() {
    return new RequestIdMdcFilter();
  }

  @Bean
  public FilterRegistrationBean<RequestIdMdcFilter> requestIdMdcFilterRegistration(
      RequestIdMdcFilter filter) {
    FilterRegistrationBean<RequestIdMdcFilter> bean = new FilterRegistrationBean<>(filter);
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return bean;
  }
}
