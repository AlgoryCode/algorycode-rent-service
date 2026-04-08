package com.algorycode.rent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.contract")
public record AppContractProperties(String templatePath, String outputDir) {}
