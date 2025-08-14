package com.bell_ringer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.bell_ringer.config")
public class BellRingerApplication {

  public static void main(String[] args) {
    SpringApplication.run(BellRingerApplication.class, args);
  }
}