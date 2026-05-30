package com.ascend.reward.repository;

import com.ascend.reward.entity.LootChest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LootChestRepository extends JpaRepository<LootChest, UUID> {
    List<LootChest> findByUserIdAndOpenedFalse(UUID userId);
}
