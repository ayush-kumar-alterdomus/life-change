package com.ascend.user.entity;

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
@Table(name = "user_stats")
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Builder.Default
    @Column(name = "strength", nullable = false)
    private Integer strength = 0;

    @Builder.Default
    @Column(name = "wisdom", nullable = false)
    private Integer wisdom = 0;

    @Builder.Default
    @Column(name = "focus", nullable = false)
    private Integer focus = 0;

    @Builder.Default
    @Column(name = "discipline", nullable = false)
    private Integer discipline = 0;

    @Builder.Default
    @Column(name = "vitality", nullable = false)
    private Integer vitality = 0;

    @Builder.Default
    @Column(name = "charisma", nullable = false)
    private Integer charisma = 0;

    @Builder.Default
    @Column(name = "life_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal lifeScore = BigDecimal.ZERO;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
