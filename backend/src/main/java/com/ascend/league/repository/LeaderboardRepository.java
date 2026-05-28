package com.ascend.league.repository;

import com.ascend.league.entity.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, UUID> {

    List<Leaderboard> findByLeagueOrderByWeeklyXpDesc(String league);

    Optional<Leaderboard> findByUserId(UUID userId);
}
