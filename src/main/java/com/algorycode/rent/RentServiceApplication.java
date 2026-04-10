package com.algorycode.rent;

import com.algorycode.rent.config.AppContractProperties;
import com.algorycode.rent.config.AppMailProperties;
import com.algorycode.rent.config.AppObjectStorageProperties;
import com.algorycode.rent.config.AppRentalRequestProperties;
import com.algorycode.rent.config.AppWhatsappProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
  AppMailProperties.class,
  AppRentalRequestProperties.class,
  AppContractProperties.class,
  AppWhatsappProperties.class,
  AppObjectStorageProperties.class
})
public class RentServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(RentServiceApplication.class, args);
  }
}
