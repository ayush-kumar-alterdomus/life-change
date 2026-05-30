package com.ascend.boss.entity;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bosses")
public class Boss {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_stages", nullable = false)
    private Integer totalStages;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stage_thresholds", columnDefinition = "jsonb")
    private List<Integer> stageThresholds;

    @Column(name = "reward_xp", nullable = false)
    private Integer rewardXp;

    @Column(name = "reward_title", length = 100)
    private String rewardTitle;

    @Column(name = "reward_cosmetic", length = 100)
    private String rewardCosmetic;

    @Builder.Default
    @Column(name = "is_guild_boss", nullable = false)
    private Boolean guildBoss = false;

    @Column(name = "arc_id")
    private UUID arcId;
}
