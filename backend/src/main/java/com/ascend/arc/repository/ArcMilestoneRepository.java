package com.ascend.arc.repository;

import com.ascend.arc.entity.ArcMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArcMilestoneRepository extends JpaRepository<ArcMilestone, UUID> {

    List<ArcMilestone> findByArcIdOrderByOrderIndex(UUID arcId);
}
