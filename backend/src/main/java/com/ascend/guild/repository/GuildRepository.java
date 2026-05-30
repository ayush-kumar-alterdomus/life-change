package com.ascend.guild.repository;

import com.ascend.guild.entity.Guild;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuildRepository extends JpaRepository<Guild, UUID> {

    boolean existsByName(String name);

    List<Guild> findByOwnerId(UUID ownerId);

    List<Guild> findByType(String type);

    Page<Guild> findByType(String type, Pageable pageable);

    Page<Guild> findAll(Pageable pageable);
}
