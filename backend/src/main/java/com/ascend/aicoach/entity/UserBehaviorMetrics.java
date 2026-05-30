package com.ascend.aicoach.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_behavior_metrics")
public class UserBehaviorMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Builder.Default
    @Column(name = "missed_quests_7d", nullable = false)
    private Integer missedQuests7d = 0;

    @Builder.Default
    @Column(name = "streak_breaks_30d", nullable = false)
    private Integer streakBreaks30d = 0;

    @Builder.Default
    @Column(name = "declining_activity_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal decliningActivityScore = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "motivation_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal motivationScore = BigDecimal.ONE;

    @Builder.Default
    @Column(name = "burnout_risk", nullable = false, precision = 5, scale = 4)
    private BigDecimal burnoutRisk = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "recovery_mode_active", nullable = false)
    private Boolean recoveryModeActive = false;

    @Column(name = "recovery_started_at")
    private LocalDateTime recoveryStartedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
