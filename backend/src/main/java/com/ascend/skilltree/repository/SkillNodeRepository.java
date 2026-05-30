package com.ascend.skilltree.repository;

import com.ascend.skilltree.entity.SkillNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SkillNodeRepository extends JpaRepository<SkillNode, UUID> {

    List<SkillNode> findByArcIdOrderByOrderIndex(UUID arcId);
}
