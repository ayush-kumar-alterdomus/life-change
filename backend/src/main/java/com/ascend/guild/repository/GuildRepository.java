package com.ascend.guild.repository;

import com.ascend.guild.entity.Guild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuildRepository extends JpaRepository<Guild, UUID> {

    List<Guild> findByOwnerId(UUID ownerId);

    List<Guild> findByType(String type);
}
