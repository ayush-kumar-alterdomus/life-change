package com.ascend.guild.repository;

import com.ascend.guild.entity.GuildChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface GuildChallengeRepository extends JpaRepository<GuildChallenge, UUID> {

    List<GuildChallenge> findByGuildIdAndEndsAtAfter(UUID guildId, LocalDateTime now);

    /**
     * Find all active (not yet ended and not yet completed) challenges for guilds
     * that a given user is a member of.
     */
    @Query("SELECT gc FROM GuildChallenge gc " +
           "WHERE gc.guildId IN (SELECT gm.guildId FROM GuildMember gm WHERE gm.userId = :userId) " +
           "AND gc.endsAt > :now " +
           "AND gc.currentProgress < gc.target")
    List<GuildChallenge> findActiveChallengesForUser(@Param("userId") UUID userId,
                                                     @Param("now") LocalDateTime now);
}
