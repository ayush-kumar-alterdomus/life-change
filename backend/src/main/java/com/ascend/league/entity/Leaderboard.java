package com.ascend.league.entity;

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
@Table(name = "leaderboard")
public class Leaderboard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Builder.Default
    @Column(name = "weekly_xp", nullable = false)
    private Long weeklyXp = 0L;

    @Column(name = "weekly_rank")
    private Integer weeklyRank;

    @Column(name = "global_rank")
    private Integer globalRank;

    @Builder.Default
    @Column(name = "league", nullable = false, length = 20)
    private String league = "BRONZE";

    @Builder.Default
    @Column(name = "consistency_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal consistencyScore = BigDecimal.ZERO;

    @Column(name = "season_id", length = 50)
    private String seasonId;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
