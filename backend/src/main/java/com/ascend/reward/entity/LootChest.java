package com.ascend.reward.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "loot_chests")
public class LootChest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "tier", nullable = false, length = 20)
    private String tier;

    @Column(name = "source", nullable = false, length = 50)
    private String source;

    @Builder.Default
    @Column(name = "opened", nullable = false)
    private Boolean opened = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contents", columnDefinition = "jsonb")
    private String contents;

    @CreationTimestamp
    @Column(name = "earned_at", nullable = false, updatable = false)
    private LocalDateTime earnedAt;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;
}
