package com.ascend.reward.repository;

import com.ascend.reward.entity.UserCosmetic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserCosmeticRepository extends JpaRepository<UserCosmetic, UUID> {
    List<UserCosmetic> findByUserId(UUID userId);
    boolean existsByUserIdAndCosmeticId(UUID userId, UUID cosmeticId);
}
