package com.gege.activitypartner.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config-path:#{null}}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        try {
            if (firebaseConfigPath == null || firebaseConfigPath.isEmpty()) {
                log.warn("Firebase configuration path not set. FCM notifications will be disabled.");
                log.warn("To enable FCM, set 'firebase.config-path' in application.properties");
                return;
            }

            InputStream serviceAccount;

            // Try to load from classpath first
            try {
                serviceAccount = new ClassPathResource(firebaseConfigPath).getInputStream();
                log.info("Loading Firebase config from classpath: {}", firebaseConfigPath);
            } catch (IOException e) {
                // If not in classpath, try absolute path
                log.info("Firebase config not found in classpath, trying absolute path: {}", firebaseConfigPath);
                serviceAccount = new FileInputStream(firebaseConfigPath);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
            log.warn("FCM notifications will be disabled. Please check your Firebase configuration.");
        }
    }
}
