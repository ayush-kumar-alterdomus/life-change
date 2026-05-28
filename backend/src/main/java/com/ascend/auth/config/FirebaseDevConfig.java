package com.ascend.auth.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Development-only configuration that provides a FirebaseAuth bean
 * when Firebase is disabled (no credentials file available).
 * This allows the application to start locally without Firebase credentials.
 *
 * Set firebase.enabled=true and provide a credentials file for real Firebase auth.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "false", matchIfMissing = true)
public class FirebaseDevConfig {

    @Bean
    public FirebaseApp firebaseApp() {
        log.warn("========================================================");
        log.warn("  Firebase is DISABLED - using mock credentials.");
        log.warn("  Token verification will NOT work.");
        log.warn("  Set firebase.enabled=true with valid credentials for");
        log.warn("  production use.");
        log.warn("========================================================");

        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.create(null))
                .setProjectId("ascend-dev-mock")
                .build();

        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }
}
