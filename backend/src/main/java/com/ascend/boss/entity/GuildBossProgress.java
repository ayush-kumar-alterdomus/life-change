package com.ascend.boss.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "guild_boss_progress", uniqueConstraints = {
        @UniqueConstraint(name = "uq_guild_boss_progress_guild_boss", columnNames = {"guild_id", "boss_id"})
})
public class GuildBossProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "guild_id", nullable = false)
    private UUID guildId;

    @Column(name = "boss_id", nullable = false)
    private UUID bossId;

    @Builder.Default
    @Column(name = "current_stage", nullable = false)
    private Integer currentStage = 1;

    @Builder.Default
    @Column(name = "progress_percent", nullable = false)
    private Integer progressPercent = 0;

    @Builder.Default
    @Column(name = "defeated", nullable = false)
    private Boolean defeated = false;

    @Column(name = "defeated_at")
    private LocalDateTime defeatedAt;
}
