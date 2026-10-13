package com.finsight.ai.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public FirebaseApp initializeFirebase() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase-service-account.json");
                if (serviceAccount == null) {
                    logger.warn("Firebase service account file (firebase-service-account.json) not found in resources. Firebase features will run in mock mode.");
                    return null;
                }
                logger.info("Initializing Firebase with service account...");
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

                FirebaseApp app = FirebaseApp.initializeApp(options);
                logger.info("Firebase initialized successfully with project ID: {}", options.getProjectId());
                return app;
            } catch (Exception e) {
                logger.warn("Could not initialize Firebase: {}. Continuing without Firebase service account.", e.getMessage());
                return null;
            }
        }
        return FirebaseApp.getInstance();
    }
}
