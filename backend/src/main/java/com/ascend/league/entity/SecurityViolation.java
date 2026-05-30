package com.ascend.league.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks anti-cheat violations detected by the system.
 * Records the type of violation, penalties applied, and resolution status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "security_violations")
public class SecurityViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "violation_type", nullable = false, length = 30)
    private ViolationType violationType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "completions_detected", nullable = false)
    private Integer completionsDetected;

    @Column(name = "time_window_minutes", nullable = false)
    private Integer timeWindowMinutes;

    @Builder.Default
    @Column(name = "xp_rolled_back", nullable = false)
    private Long xpRolledBack = 0L;

    @Builder.Default
    @Column(name = "leaderboard_banned", nullable = false)
    private Boolean leaderboardBanned = false;

    @Builder.Default
    @Column(name = "resolved", nullable = false)
    private Boolean resolved = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
