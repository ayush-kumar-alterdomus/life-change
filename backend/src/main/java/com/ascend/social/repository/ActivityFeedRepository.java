package com.ascend.social.repository;

import com.ascend.social.model.ActivityFeed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityFeedRepository extends JpaRepository<ActivityFeed, UUID> {

    Page<ActivityFeed> findByUserIdInOrderByCreatedAtDesc(List<UUID> userIds, Pageable pageable);
}
