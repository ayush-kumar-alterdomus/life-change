package com.ascend.boss.repository;

import com.ascend.boss.entity.GuildBossProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GuildBossProgressRepository extends JpaRepository<GuildBossProgress, UUID> {

    Optional<GuildBossProgress> findByGuildIdAndBossId(UUID guildId, UUID bossId);

    List<GuildBossProgress> findByGuildId(UUID guildId);

    List<GuildBossProgress> findByGuildIdAndDefeatedFalse(UUID guildId);
}
