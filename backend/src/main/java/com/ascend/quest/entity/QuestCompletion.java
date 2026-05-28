package com.ascend.quest.entity;

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
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quest_completion", uniqueConstraints = {
        @UniqueConstraint(name = "uq_quest_completion_user_quest_day",
                columnNames = {"user_id", "quest_id", "completed_at"})
})
public class QuestCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "quest_id", nullable = false)
    private UUID questId;

    @CreationTimestamp
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "xp_earned", nullable = false)
    private Integer xpEarned;

    @Column(name = "multiplier", precision = 10, scale = 2)
    private BigDecimal multiplier;

    @Column(name = "difficulty_at_completion", length = 20)
    private String difficultyAtCompletion;
}
