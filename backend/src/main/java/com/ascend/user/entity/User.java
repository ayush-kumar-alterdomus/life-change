package com.ascend.user.entity;

import com.ascend.auth.entity.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "firebase_uid", unique = true, nullable = false, length = 255)
    private String firebaseUid;

    @Column(name = "username", unique = true, length = 50)
    private String username;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Builder.Default
    @Column(name = "level", nullable = false)
    private Integer level = 1;

    @Builder.Default
    @Column(name = "xp", nullable = false)
    private Long xp = 0L;

    @Builder.Default
    @Column(name = "league", nullable = false, length = 20)
    private String league = "BRONZE";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Builder.Default
    @Column(name = "premium", nullable = false)
    private Boolean premium = false;

    @Builder.Default
    @Column(name = "prestige_level", nullable = false)
    private Integer prestigeLevel = 0;

    @Builder.Default
    @Column(name = "hard_mode", nullable = false)
    private Boolean hardMode = false;

    @Builder.Default
    @Column(name = "is_guest", nullable = false)
    private Boolean guest = false;

    @Builder.Default
    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "UTC";

    @Column(name = "last_skill_reset_at")
    private LocalDateTime lastSkillResetAt;

    @Builder.Default
    @Column(name = "suspended", nullable = false)
    private Boolean suspended = false;

    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    @Builder.Default
    @Column(name = "banned", nullable = false)
    private Boolean banned = false;

    @Builder.Default
    @Column(name = "flagged", nullable = false)
    private Boolean flagged = false;

    @Builder.Default
    @Column(name = "privacy_level", nullable = false, length = 20)
    private String privacyLevel = "PUBLIC";

    @Column(name = "fcm_token", length = 512)
    private String fcmToken;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notification_preferences", columnDefinition = "jsonb")
    private String notificationPreferences;

    @Builder.Default
    @Column(name = "onboarding_complete", nullable = false)
    private Boolean onboardingComplete = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selected_goals", columnDefinition = "jsonb")
    private String selectedGoals;

    @Column(name = "personality_type", length = 30)
    private String personalityType;

    @Column(name = "difficulty_preference", length = 20)
    private String difficultyPreference;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
