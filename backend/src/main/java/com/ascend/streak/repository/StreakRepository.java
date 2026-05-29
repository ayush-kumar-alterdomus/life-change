package com.ascend.streak.repository;

import com.ascend.streak.entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StreakRepository extends JpaRepository<Streak, UUID> {

    Optional<Streak> findByUserId(UUID userId);

    List<Streak> findByComebackModeActiveTrue();
}
