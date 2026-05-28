package com.ascend.guild.repository;

import com.ascend.guild.entity.GuildChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface GuildChallengeRepository extends JpaRepository<GuildChallenge, UUID> {

    List<GuildChallenge> findByGuildIdAndEndsAtAfter(UUID guildId, LocalDateTime now);
}
