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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "firebase_uid", unique = true, nullable = false, length = 255)
    private String firebaseUid;

    @Column(name = "username", unique = true, length = 50)
    private String username;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Builder.Default
    @Column(name = "level", nullable = false)
    private Integer level = 1;

    @Builder.Default
    @Column(name = "xp", nullable = false)
    private Long xp = 0L;

    @Builder.Default
    @Column(name = "league", nullable = false, length = 20)
    private String league = "BRONZE";

    @Builder.Default
    @Column(name = "premium", nullable = false)
    private Boolean premium = false;

    @Builder.Default
    @Column(name = "hard_mode", nullable = false)
    private Boolean hardMode = false;

    @Builder.Default
    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "UTC";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
