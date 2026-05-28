package com.ascend.quest.service;

import com.ascend.arc.entity.UserArcProgress;
import com.ascend.arc.repository.UserArcProgressRepository;
import com.ascend.common.entity.Frequency;
import com.ascend.common.entity.StatType;
import com.ascend.quest.entity.Quest;
import com.ascend.quest.repository.QuestRepository;
import com.ascend.user.entity.User;
import com.ascend.user.entity.UserStats;
import com.ascend.user.repository.UserStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for assigning daily quests to users during the daily reset.
 * <p>
 * Assignment logic:
 * - Arc users: receive Arc-specific daily quests tied to their active arc
 * - Non-Arc users: receive default daily missions based on their stat preferences
 * (lowest stats get priority to encourage balanced growth)
 * <p>
 * Note: Quest availability is determined by the absence of a completion record
 * for the current day. This service ensures the quest pool is populated, not
 * that completion records are cleared.
 */
@Slf4j
@Service
public class QuestAssignmentService {

    private static final int DEFAULT_DAILY_QUEST_COUNT = 5;

    private final QuestRepository questRepository;
    private final UserArcProgressRepository userArcProgressRepository;
    private final UserStatsRepository userStatsRepository;

    public QuestAssignmentService(QuestRepository questRepository,
                                  UserArcProgressRepository userArcProgressRepository,
                                  UserStatsRepository userStatsRepository) {
        this.questRepository = questRepository;
        this.userArcProgressRepository = userArcProgressRepository;
        this.userStatsRepository = userStatsRepository;
    }

    /**
     * Assigns daily quests for a user based on their current context.
     * Arc users get arc-specific quests; non-arc users get stat-preference-based quests.
     *
     * @param user the user to assign quests to
     */
    @Transactional
    public void assignDailyQuests(User user) {
        UUID userId = user.getId();
        log.debug("Assigning daily quests for user={}", userId);

        Optional<UserArcProgress> activeArc = getActiveArc(userId);

        if (activeArc.isPresent()) {
            assignArcQuests(userId, activeArc.get().getArcId());
        } else {
            assignDefaultQuests(userId);
        }
    }

    /**
     * Assigns Arc-specific daily quests for users enrolled in an active arc.
     * Fetches daily-frequency quests tied to the user's active arc.
     */
    private void assignArcQuests(UUID userId, UUID arcId) {
        List<Quest> arcDailyQuests = questRepository.findByArcIdAndFrequency(arcId, Frequency.DAILY);

        if (arcDailyQuests.isEmpty()) {
            log.warn("No daily quests found for arc={}, falling back to default assignment for user={}",
                    arcId, userId);
            assignDefaultQuests(userId);
            return;
        }

        log.debug("Assigned {} arc daily quests for user={}, arc={}",
                arcDailyQuests.size(), userId, arcId);
    }

    /**
     * Assigns default daily missions for non-Arc users based on their stat preferences.
     * Prioritizes quests for the user's weakest stats to encourage balanced growth.
     */
    private void assignDefaultQuests(UUID userId) {
        Optional<UserStats> userStatsOpt = userStatsRepository.findByUserId(userId);

        List<Quest> dailyQuests;

        if (userStatsOpt.isPresent()) {
            List<StatType> preferredStats = getWeakestStats(userStatsOpt.get());
            dailyQuests = questRepository.findByStatTypeInAndFrequencyAndCustomFalse(
                    preferredStats, Frequency.DAILY);

            // If not enough quests match preferred stats, supplement with general daily quests
            if (dailyQuests.size() < DEFAULT_DAILY_QUEST_COUNT) {
                List<Quest> generalQuests = questRepository.findByFrequencyAndCustomFalse(Frequency.DAILY);
                List<UUID> existingIds = dailyQuests.stream()
                        .map(Quest::getId)
                        .toList();

                generalQuests.stream()
                        .filter(q -> !existingIds.contains(q.getId()))
                        .limit((long) DEFAULT_DAILY_QUEST_COUNT - dailyQuests.size())
                        .forEach(dailyQuests::add);
            }
        } else {
            // No stats record — assign general daily quests
            dailyQuests = questRepository.findByFrequencyAndCustomFalse(Frequency.DAILY);
        }

        // Cap at the default daily quest count
        if (dailyQuests.size() > DEFAULT_DAILY_QUEST_COUNT) {
            dailyQuests = dailyQuests.subList(0, DEFAULT_DAILY_QUEST_COUNT);
        }

        log.debug("Assigned {} default daily quests for user={}", dailyQuests.size(), userId);
    }

    /**
     * Finds the user's active arc progress (status = "ACTIVE").
     * Returns the first active arc if multiple exist.
     */
    private Optional<UserArcProgress> getActiveArc(UUID userId) {
        List<UserArcProgress> activeArcs = userArcProgressRepository
                .findByUserIdAndStatus(userId, "ACTIVE");
        return activeArcs.isEmpty() ? Optional.empty() : Optional.of(activeArcs.get(0));
    }

    /**
     * Determines the user's weakest stats to prioritize quest assignment.
     * Returns the 3 lowest stat types to provide a balanced quest mix.
     */
    List<StatType> getWeakestStats(UserStats stats) {
        List<Map.Entry<StatType, Integer>> statEntries = new ArrayList<>();
        statEntries.add(Map.entry(StatType.STRENGTH, stats.getStrength()));
        statEntries.add(Map.entry(StatType.WISDOM, stats.getWisdom()));
        statEntries.add(Map.entry(StatType.FOCUS, stats.getFocus()));
        statEntries.add(Map.entry(StatType.DISCIPLINE, stats.getDiscipline()));
        statEntries.add(Map.entry(StatType.VITALITY, stats.getVitality()));
        statEntries.add(Map.entry(StatType.CHARISMA, stats.getCharisma()));

        statEntries.sort(Comparator.comparingInt(Map.Entry::getValue));

        // Return the 3 weakest stat types
        return statEntries.stream()
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }
}
