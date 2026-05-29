package com.ascend.streak.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "streaks")
public class Streak {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Builder.Default
    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak = 0;

    @Builder.Default
    @Column(name = "longest_streak", nullable = false)
    private Integer longestStreak = 0;

    @Builder.Default
    @Column(name = "combo_multiplier", nullable = false, precision = 10, scale = 2)
    private BigDecimal comboMultiplier = BigDecimal.ONE;

    @Column(name = "last_completed_at")
    private LocalDateTime lastCompletedAt;

    @Builder.Default
    @Column(name = "shield_available", nullable = false)
    private Boolean shieldAvailable = false;

    @Column(name = "shield_used_at")
    private LocalDateTime shieldUsedAt;

    @Builder.Default
    @Column(name = "comeback_mode_active", nullable = false)
    private Boolean comebackModeActive = false;

    @Column(name = "comeback_expires_at")
    private LocalDateTime comebackExpiresAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
