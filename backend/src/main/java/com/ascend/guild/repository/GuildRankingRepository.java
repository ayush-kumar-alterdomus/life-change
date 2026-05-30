package com.ascend.guild.repository;

import com.ascend.guild.entity.GuildRanking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GuildRankingRepository extends JpaRepository<GuildRanking, UUID> {

    Optional<GuildRanking> findByGuildId(UUID guildId);

    Page<GuildRanking> findAllByOrderByRankAsc(Pageable pageable);
}
