package com.ascend.skilltree.repository;

import com.ascend.skilltree.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {

    List<UserSkill> findByUserIdAndArcId(UUID userId, UUID arcId);

    List<UserSkill> findByUserIdAndUnlockedTrue(UUID userId);
}
