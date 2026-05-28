package com.ascend.auth.service;

import com.ascend.auth.entity.SecurityEvent;
import com.ascend.auth.repository.SecurityEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for logging security audit events.
 * Captures login attempts, permission denials, rate limit violations, and anti-cheat flags.
 * All PII (emails) is masked before persistence.
 */
@Slf4j
@Service
public class SecurityAuditService {

    public static final String EVENT_LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String EVENT_LOGIN_FAILURE = "LOGIN_FAILURE";
    public static final String EVENT_PERMISSION_DENIED = "PERMISSION_DENIED";
    public static final String EVENT_RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String EVENT_ANTI_CHEAT_FLAG = "ANTI_CHEAT_FLAG";

    private final SecurityEventRepository securityEventRepository;

    public SecurityAuditService(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    /**
     * Logs a successful login attempt.
     */
    @Async
    public void logLoginSuccess(UUID userId, String email, String ipAddress, String provider) {
        Map<String, Object> details = new HashMap<>();
        details.put("email", maskEmail(email));
        details.put("provider", provider);

        persistEvent(userId, EVENT_LOGIN_SUCCESS, ipAddress, details);
    }

    /**
     * Logs a failed login attempt.
     */
    @Async
    public void logLoginFailure(String email, String ipAddress, String reason) {
        Map<String, Object> details = new HashMap<>();
        details.put("email", maskEmail(email));
        details.put("reason", reason);

        persistEvent(null, EVENT_LOGIN_FAILURE, ipAddress, details);
    }

    /**
     * Logs a permission denial event.
     */
    @Async
    public void logPermissionDenied(UUID userId, String ipAddress, String resource, String requiredRole) {
        Map<String, Object> details = new HashMap<>();
        details.put("resource", resource);
        details.put("requiredRole", requiredRole);

        persistEvent(userId, EVENT_PERMISSION_DENIED, ipAddress, details);
    }

    /**
     * Logs a rate limit violation.
     */
    @Async
    public void logRateLimitViolation(UUID userId, String ipAddress, String bucket, String clientKey) {
        Map<String, Object> details = new HashMap<>();
        details.put("bucket", bucket);
        details.put("clientKey", clientKey);

        persistEvent(userId, EVENT_RATE_LIMIT_EXCEEDED, ipAddress, details);
    }

    /**
     * Logs an anti-cheat flag event.
     */
    @Async
    public void logAntiCheatFlag(UUID userId, String ipAddress, String flagType, String description) {
        Map<String, Object> details = new HashMap<>();
        details.put("flagType", flagType);
        details.put("description", description);

        persistEvent(userId, EVENT_ANTI_CHEAT_FLAG, ipAddress, details);
    }

    /**
     * Persists a security event to the database.
     */
    private void persistEvent(UUID userId, String eventType, String ipAddress, Map<String, Object> details) {
        try {
            SecurityEvent event = SecurityEvent.builder()
                    .userId(userId)
                    .eventType(eventType)
                    .ipAddress(ipAddress)
                    .details(details)
                    .build();

            securityEventRepository.save(event);
            log.debug("Security event logged: type={}, userId={}, ip={}", eventType, userId, ipAddress);
        } catch (Exception e) {
            // Security logging should never break the main flow
            log.error("Failed to persist security event: type={}, error={}", eventType, e.getMessage());
        }
    }

    /**
     * Masks an email address for PII protection.
     * Example: "user@example.com" → "u***@example.com"
     * Null or invalid emails return a placeholder.
     */
    static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "***";
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (localPart.length() == 1) {
            return localPart.charAt(0) + "***" + domain;
        }

        return localPart.charAt(0) + "***" + domain;
    }
}
