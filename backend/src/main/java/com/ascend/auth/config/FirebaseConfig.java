package com.ascend.auth.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = false)
public class FirebaseConfig {

    @Value("${firebase.credentials-file}")
    private Resource credentialsFile;

    @Value("${firebase.project-id}")
    private String projectId;

    @Bean
    public FirebaseApp firebaseApp() {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.debug("FirebaseApp already initialized, reusing existing instance");
            return FirebaseApp.getInstance();
        }

        if (!StringUtils.hasText(projectId)) {
            throw new IllegalStateException(
                    "Firebase project-id is not configured. Set 'firebase.project-id' in application properties.");
        }

        if (!credentialsFile.exists()) {
            throw new IllegalStateException(
                    "Firebase credentials file not found. Ensure 'firebase.credentials-file' is configured correctly.");
        }

        try (InputStream stream = credentialsFile.getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized for project: {}", projectId);
            return app;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize Firebase from credentials", e);
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }
}
