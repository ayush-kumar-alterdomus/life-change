package com.ascend.auth.service;

import com.ascend.auth.entity.SecurityEvent;
import com.ascend.auth.repository.SecurityEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityAuditServiceTest {

    @Mock
    private SecurityEventRepository securityEventRepository;

    private SecurityAuditService securityAuditService;

    @BeforeEach
    void setUp() {
        securityAuditService = new SecurityAuditService(securityEventRepository);
    }

    @Test
    void maskEmail_standardEmail_masksLocalPart() {
        String result = SecurityAuditService.maskEmail("user@example.com");
        assertThat(result).isEqualTo("u***@example.com");
    }

    @Test
    void maskEmail_singleCharLocalPart_masksCorrectly() {
        String result = SecurityAuditService.maskEmail("a@example.com");
        assertThat(result).isEqualTo("a***@example.com");
    }

    @Test
    void maskEmail_nullEmail_returnsPlaceholder() {
        String result = SecurityAuditService.maskEmail(null);
        assertThat(result).isEqualTo("***");
    }

    @Test
    void maskEmail_emptyEmail_returnsPlaceholder() {
        String result = SecurityAuditService.maskEmail("");
        assertThat(result).isEqualTo("***");
    }

    @Test
    void maskEmail_noAtSign_returnsPlaceholder() {
        String result = SecurityAuditService.maskEmail("invalidemail");
        assertThat(result).isEqualTo("***");
    }

    @Test
    void logLoginSuccess_persistsEventWithCorrectType() {
        UUID userId = UUID.randomUUID();
        when(securityEventRepository.save(any(SecurityEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        securityAuditService.logLoginSuccess(userId, "test@example.com", "192.168.1.1", "google.com");

        ArgumentCaptor<SecurityEvent> captor = ArgumentCaptor.forClass(SecurityEvent.class);
        verify(securityEventRepository).save(captor.capture());

        SecurityEvent saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getEventType()).isEqualTo(SecurityAuditService.EVENT_LOGIN_SUCCESS);
        assertThat(saved.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(saved.getDetails()).containsEntry("email", "t***@example.com");
        assertThat(saved.getDetails()).containsEntry("provider", "google.com");
    }

    @Test
    void logLoginFailure_persistsEventWithNullUserId() {
        when(securityEventRepository.save(any(SecurityEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        securityAuditService.logLoginFailure("user@test.com", "10.0.0.1", "Invalid token");

        ArgumentCaptor<SecurityEvent> captor = ArgumentCaptor.forClass(SecurityEvent.class);
        verify(securityEventRepository).save(captor.capture());

        SecurityEvent saved = captor.getValue();
        assertThat(saved.getUserId()).isNull();
        assertThat(saved.getEventType()).isEqualTo(SecurityAuditService.EVENT_LOGIN_FAILURE);
        assertThat(saved.getIpAddress()).isEqualTo("10.0.0.1");
        assertThat(saved.getDetails()).containsEntry("email", "u***@test.com");
        assertThat(saved.getDetails()).containsEntry("reason", "Invalid token");
    }

    @Test
    void logPermissionDenied_persistsEventWithResourceDetails() {
        UUID userId = UUID.randomUUID();
        when(securityEventRepository.save(any(SecurityEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        securityAuditService.logPermissionDenied(userId, "172.16.0.1", "/api/v1/admin/users", "ADMIN");

        ArgumentCaptor<SecurityEvent> captor = ArgumentCaptor.forClass(SecurityEvent.class);
        verify(securityEventRepository).save(captor.capture());

        SecurityEvent saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getEventType()).isEqualTo(SecurityAuditService.EVENT_PERMISSION_DENIED);
        assertThat(saved.getDetails()).containsEntry("resource", "/api/v1/admin/users");
        assertThat(saved.getDetails()).containsEntry("requiredRole", "ADMIN");
    }

    @Test
    void logRateLimitViolation_persistsEventWithBucketDetails() {
        UUID userId = UUID.randomUUID();
        when(securityEventRepository.save(any(SecurityEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        securityAuditService.logRateLimitViolation(userId, "192.168.1.100", "auth", "user:abc123");

        ArgumentCaptor<SecurityEvent> captor = ArgumentCaptor.forClass(SecurityEvent.class);
        verify(securityEventRepository).save(captor.capture());

        SecurityEvent saved = captor.getValue();
        assertThat(saved.getEventType()).isEqualTo(SecurityAuditService.EVENT_RATE_LIMIT_EXCEEDED);
        assertThat(saved.getDetails()).containsEntry("bucket", "auth");
        assertThat(saved.getDetails()).containsEntry("clientKey", "user:abc123");
    }

    @Test
    void logAntiCheatFlag_persistsEventWithFlagDetails() {
        UUID userId = UUID.randomUUID();
        when(securityEventRepository.save(any(SecurityEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        securityAuditService.logAntiCheatFlag(userId, "10.0.0.5", "RAPID_XP", "Gained 5000 XP in 1 minute");

        ArgumentCaptor<SecurityEvent> captor = ArgumentCaptor.forClass(SecurityEvent.class);
        verify(securityEventRepository).save(captor.capture());

        SecurityEvent saved = captor.getValue();
        assertThat(saved.getEventType()).isEqualTo(SecurityAuditService.EVENT_ANTI_CHEAT_FLAG);
        assertThat(saved.getDetails()).containsEntry("flagType", "RAPID_XP");
        assertThat(saved.getDetails()).containsEntry("description", "Gained 5000 XP in 1 minute");
    }
}
