package com.ascend.league.repository;

import com.ascend.league.entity.Leaderboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, UUID> {

    List<Leaderboard> findByLeagueOrderByWeeklyXpDesc(String league);

    Page<Leaderboard> findByLeagueOrderByWeeklyXpDesc(String league, Pageable pageable);

    Optional<Leaderboard> findByUserId(UUID userId);

    long countByLeague(String league);

    @Modifying
    @Query("UPDATE Leaderboard l SET l.weeklyXp = 0, l.weeklyRank = null")
    void resetAllWeeklyXp();
}
