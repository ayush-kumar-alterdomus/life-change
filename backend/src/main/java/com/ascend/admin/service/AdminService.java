package com.ascend.admin.service;

import com.ascend.arc.entity.Arc;
import com.ascend.arc.repository.ArcRepository;
import com.ascend.boss.entity.Boss;
import com.ascend.boss.repository.BossRepository;
import com.ascend.quest.entity.Quest;
import com.ascend.quest.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * Admin service for content management (Arcs, Quests, Bosses).
 * All operations invalidate relevant Redis caches.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final ArcRepository arcRepository;
    private final QuestRepository questRepository;
    private final BossRepository bossRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public Arc createArc(Arc arc) {
        Arc saved = arcRepository.save(arc);
        invalidateCache("arcs:*");
        log.info("Admin created arc: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Transactional
    public Arc updateArc(UUID arcId, Arc updates) {
        Arc arc = arcRepository.findById(arcId)
                .orElseThrow(() -> new IllegalArgumentException("Arc not found: " + arcId));
        if (updates.getName() != null) arc.setName(updates.getName());
        if (updates.getDescription() != null) arc.setDescription(updates.getDescription());
        Arc saved = arcRepository.save(arc);
        invalidateCache("arcs:*");
        log.info("Admin updated arc: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Transactional
    public Quest createQuest(Quest quest) {
        Quest saved = questRepository.save(quest);
        invalidateCache("quests:*");
        log.info("Admin created quest: {} ({})", saved.getTitle(), saved.getId());
        return saved;
    }

    @Transactional
    public Quest updateQuest(UUID questId, Quest updates) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + questId));
        if (updates.getTitle() != null) quest.setTitle(updates.getTitle());
        if (updates.getDescription() != null) quest.setDescription(updates.getDescription());
        Quest saved = questRepository.save(quest);
        invalidateCache("quests:*");
        log.info("Admin updated quest: {} ({})", saved.getTitle(), saved.getId());
        return saved;
    }

    @Transactional
    public Boss createBoss(Boss boss) {
        Boss saved = bossRepository.save(boss);
        invalidateCache("bosses:*");
        log.info("Admin created boss: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    private void invalidateCache(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Invalidated {} cache keys matching '{}'", keys.size(), pattern);
            }
        } catch (Exception e) {
            log.warn("Failed to invalidate cache for pattern '{}': {}", pattern, e.getMessage());
        }
    }
}
