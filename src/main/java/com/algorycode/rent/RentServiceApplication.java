package com.algorycode.rent;

import com.algorycode.rent.config.AppMailProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppMailProperties.class)
public class RentServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(RentServiceApplication.class, args);
  }
}
