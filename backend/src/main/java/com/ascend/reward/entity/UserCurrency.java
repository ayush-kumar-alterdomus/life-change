package com.ascend.reward.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_currency")
public class UserCurrency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Builder.Default
    @Column(name = "coins", nullable = false)
    private Long coins = 0L;

    @Builder.Default
    @Column(name = "gems", nullable = false)
    private Long gems = 0L;

    @Builder.Default
    @Column(name = "daily_coins_earned", nullable = false)
    private Long dailyCoinsEarned = 0L;

    @Builder.Default
    @Column(name = "daily_reset_at", nullable = false)
    private LocalDateTime dailyResetAt = LocalDateTime.now();

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
