package com.bell_ringer.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@ConditionalOnResource(resources = "classpath:serviceAccountKey.json")
public class FirebaseConfig {

  private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

  @Bean
  public FirebaseApp initializeFirebase() throws IOException {
    logger.info("Initializing Firebase with service account key...");

    // If already initialized, return the existing DEFAULT app
    if (!FirebaseApp.getApps().isEmpty()) {
      return FirebaseApp.getInstance();
    }

    try (InputStream serviceAccount = new ClassPathResource("serviceAccountKey.json").getInputStream()) {
      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(GoogleCredentials.fromStream(serviceAccount))
          .build();
      return FirebaseApp.initializeApp(options);
    }
  }

  @Bean
  public FirebaseAuth firebaseAuth(FirebaseApp app) {
    return FirebaseAuth.getInstance(app);
  }
}