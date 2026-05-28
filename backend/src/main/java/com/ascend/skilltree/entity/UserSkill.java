package com.ascend.skilltree.entity;

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
@Table(name = "user_skills", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_skills_user_skill", columnNames = {"user_id", "skill_id"})
})
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "skill_id", nullable = false)
    private UUID skillId;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(name = "arc_id")
    private UUID arcId;

    @Builder.Default
    @Column(name = "unlocked", nullable = false)
    private Boolean unlocked = false;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;
}
