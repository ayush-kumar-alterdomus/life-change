package com.ascend.guild.entity;

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

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "guild_rankings")
public class GuildRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "guild_id", unique = true, nullable = false)
    private UUID guildId;

    @Builder.Default
    @Column(name = "avg_consistency", nullable = false)
    private Double avgConsistency = 0.0;

    @Builder.Default
    @Column(name = "total_quests_completed", nullable = false)
    private Long totalQuestsCompleted = 0L;

    @Builder.Default
    @Column(name = "avg_streak", nullable = false)
    private Double avgStreak = 0.0;

    @Builder.Default
    @Column(name = "score", nullable = false)
    private Double score = 0.0;

    @Column(name = "rank")
    private Integer rank;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
