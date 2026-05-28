package com.ascend.boss.repository;

import com.ascend.boss.entity.BossProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BossProgressRepository extends JpaRepository<BossProgress, UUID> {

    List<BossProgress> findByUserIdAndDefeatedFalse(UUID userId);

    Optional<BossProgress> findByUserIdAndBossId(UUID userId, UUID bossId);
}
