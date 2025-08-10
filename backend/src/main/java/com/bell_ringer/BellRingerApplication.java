package com.bell_ringer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.bell_ringer.config")
@RestController
public class BellRingerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BellRingerApplication.class, args);
	}

  @GetMapping
  public String index() {
    return "Spring Boot application running";
  }
}