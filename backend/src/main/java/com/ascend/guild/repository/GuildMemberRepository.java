package com.ascend.guild.repository;

import com.ascend.guild.entity.GuildMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuildMemberRepository extends JpaRepository<GuildMember, UUID> {

    List<GuildMember> findByGuildId(UUID guildId);

    List<GuildMember> findByUserId(UUID userId);

    long countByGuildId(UUID guildId);
}
