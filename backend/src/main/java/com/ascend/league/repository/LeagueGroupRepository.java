package com.ascend.league.repository;

import com.ascend.league.entity.LeagueGroup;
import com.ascend.league.entity.LeagueTier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeagueGroupRepository extends JpaRepository<LeagueGroup, UUID> {

    Optional<LeagueGroup> findByUserId(UUID userId);

    List<LeagueGroup> findByGroupId(UUID groupId);

    List<LeagueGroup> findByGroupIdOrderByLeagueScoreDesc(UUID groupId);

    Page<LeagueGroup> findByTierOrderByLeagueScoreDesc(LeagueTier tier, Pageable pageable);

    List<LeagueGroup> findByTierAndSeasonWeek(LeagueTier tier, String seasonWeek);

    long countByGroupId(UUID groupId);

    @Query("SELECT DISTINCT lg.groupId FROM LeagueGroup lg WHERE lg.seasonWeek = :seasonWeek")
    List<UUID> findDistinctGroupIdsBySeasonWeek(@Param("seasonWeek") String seasonWeek);

    List<LeagueGroup> findBySeasonWeek(String seasonWeek);

    @Modifying
    @Query("DELETE FROM LeagueGroup lg WHERE lg.seasonWeek = :seasonWeek")
    void deleteBySeasonWeek(@Param("seasonWeek") String seasonWeek);
}
