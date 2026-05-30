package com.ascend.social.repository;

import com.ascend.social.model.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {

    @Query("SELECT c FROM Challenge c WHERE (c.challenger.id = :userId OR c.challenged.id = :userId) AND c.status = 'ACTIVE'")
    List<Challenge> findActiveChallenges(@Param("userId") UUID userId);
}
