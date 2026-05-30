package com.ascend.aicoach.service;

import com.ascend.quest.repository.QuestCompletionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdaptiveDifficultyService {

    private final QuestCompletionRepository completionRepository;

    /**
     * Returns difficulty adjustment: "INCREASE", "DECREASE", or "MAINTAIN".
     */
    public String adjustDifficulty(UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        long completions14d = completionRepository
                .findByUserIdAndCompletedAtBetween(userId, now.minusDays(14), now).size();

        // Assume ~2 quests/day target = 28 over 14 days
        double completionRate = completions14d / 28.0;

        if (completionRate > 0.9) {
            log.debug("User {} completing >90%, suggesting harder quests", userId);
            return "INCREASE";
        } else if (completionRate < 0.5) {
            log.debug("User {} completing <50%, suggesting easier quests", userId);
            return "DECREASE";
        }

        return "MAINTAIN";
    }
}
