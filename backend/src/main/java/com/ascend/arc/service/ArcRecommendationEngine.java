package com.ascend.arc.service;

import com.ascend.arc.dto.ArcResponse;
import com.ascend.arc.dto.ArcType;
import com.ascend.arc.entity.Arc;
import com.ascend.arc.entity.ArcMilestone;
import com.ascend.arc.repository.ArcMilestoneRepository;
import com.ascend.arc.repository.ArcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Rule-based recommendation engine for suggesting arcs to users
 * based on their goals, assessment answers, and available time.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArcRecommendationEngine {

    private final ArcRepository arcRepository;
    private final ArcMilestoneRepository arcMilestoneRepository;

    // Goal-to-ArcType mapping for rule-based matching
    private static final Map<String, ArcType> GOAL_TYPE_MAP = new HashMap<>();

    static {
        GOAL_TYPE_MAP.put("fitness", ArcType.WARRIOR);
        GOAL_TYPE_MAP.put("exercise", ArcType.WARRIOR);
        GOAL_TYPE_MAP.put("strength", ArcType.WARRIOR);
        GOAL_TYPE_MAP.put("health", ArcType.WARRIOR);
        GOAL_TYPE_MAP.put("learning", ArcType.SCHOLAR);
        GOAL_TYPE_MAP.put("study", ArcType.SCHOLAR);
        GOAL_TYPE_MAP.put("reading", ArcType.SCHOLAR);
        GOAL_TYPE_MAP.put("knowledge", ArcType.SCHOLAR);
        GOAL_TYPE_MAP.put("meditation", ArcType.MONK);
        GOAL_TYPE_MAP.put("mindfulness", ArcType.MONK);
        GOAL_TYPE_MAP.put("discipline", ArcType.MONK);
        GOAL_TYPE_MAP.put("focus", ArcType.MONK);
        GOAL_TYPE_MAP.put("creativity", ArcType.CREATOR);
        GOAL_TYPE_MAP.put("art", ArcType.CREATOR);
        GOAL_TYPE_MAP.put("writing", ArcType.CREATOR);
        GOAL_TYPE_MAP.put("music", ArcType.CREATOR);
        GOAL_TYPE_MAP.put("intensity", ArcType.BEAST_MODE);
        GOAL_TYPE_MAP.put("challenge", ArcType.BEAST_MODE);
        GOAL_TYPE_MAP.put("extreme", ArcType.BEAST_MODE);
        GOAL_TYPE_MAP.put("hardcore", ArcType.BEAST_MODE);
    }

    /**
     * Recommends arcs based on user goals, assessment answers, and available time.
     * Uses simple rule-based matching (not ML) for MVP.
     *
     * @param goals                  list of user goal keywords
     * @param assessmentAnswers      map of assessment question keys to answers
     * @param availableMinutesPerDay minutes the user can dedicate per day
     * @return list of recommended arcs, sorted by relevance score (top recommendation first)
     */
    @Transactional(readOnly = true)
    public List<ArcResponse> recommend(List<String> goals, Map<String, Object> assessmentAnswers,
                                       int availableMinutesPerDay) {
        log.info("Generating arc recommendations for goals={}, availableMinutes={}", goals, availableMinutesPerDay);

        // 1. Map goals to Arc types
        List<ArcType> targetTypes = mapGoalsToTypes(goals);

        // 2. Determine difficulty from assessment
        String targetDifficulty = determineDifficulty(assessmentAnswers, availableMinutesPerDay);

        // 3. Fetch all prebuilt arcs
        List<Arc> allArcs = arcRepository.findByPrebuiltTrue();

        // 4. Score and rank arcs
        List<ScoredArc> scoredArcs = allArcs.stream()
                .map(arc -> new ScoredArc(arc, scoreArc(arc, targetTypes, targetDifficulty)))
                .sorted(Comparator.comparingInt(ScoredArc::score).reversed())
                .collect(Collectors.toList());

        // 5. Return top recommendations (max 5)
        return scoredArcs.stream()
                .limit(5)
                .map(scored -> toArcResponse(scored.arc()))
                .collect(Collectors.toList());
    }

    /**
     * Maps user goal strings to ArcType enums using keyword matching.
     */
    private List<ArcType> mapGoalsToTypes(List<String> goals) {
        List<ArcType> types = new ArrayList<>();
        for (String goal : goals) {
            String lowerGoal = goal.toLowerCase().trim();
            for (Map.Entry<String, ArcType> entry : GOAL_TYPE_MAP.entrySet()) {
                if (lowerGoal.contains(entry.getKey())) {
                    types.add(entry.getValue());
                    break;
                }
            }
        }
        if (types.isEmpty()) {
            // Default to MONK if no goals match
            types.add(ArcType.MONK);
        }
        return types;
    }

    /**
     * Determines target difficulty based on assessment answers and available time.
     */
    private String determineDifficulty(Map<String, Object> assessmentAnswers, int availableMinutesPerDay) {
        // Simple heuristic based on available time and experience level
        Object experienceLevel = assessmentAnswers.getOrDefault("experienceLevel", "beginner");
        String experience = experienceLevel.toString().toLowerCase();

        if (availableMinutesPerDay >= 60 && "advanced".equals(experience)) {
            return "HARD";
        } else if (availableMinutesPerDay >= 30 && !"beginner".equals(experience)) {
            return "MEDIUM";
        }
        return "EASY";
    }

    /**
     * Scores an arc based on goal alignment and difficulty match.
     */
    private int scoreArc(Arc arc, List<ArcType> targetTypes, String targetDifficulty) {
        int score = 0;

        // Goal alignment: +10 points for type match
        if (arc.getType() != null) {
            try {
                ArcType arcType = ArcType.valueOf(arc.getType().toUpperCase());
                if (targetTypes.contains(arcType)) {
                    score += 10;
                }
            } catch (IllegalArgumentException ignored) {
                // Unknown type, no bonus
            }
        }

        // Difficulty match: +5 points for exact match, +2 for adjacent
        if (targetDifficulty.equalsIgnoreCase(arc.getDifficulty())) {
            score += 5;
        } else if (isAdjacentDifficulty(targetDifficulty, arc.getDifficulty())) {
            score += 2;
        }

        // Base score for prebuilt arcs (always at least 1)
        score += 1;

        return score;
    }

    /**
     * Checks if two difficulty levels are adjacent (e.g., EASY-MEDIUM or MEDIUM-HARD).
     */
    private boolean isAdjacentDifficulty(String target, String actual) {
        if (target == null || actual == null) return false;
        String t = target.toUpperCase();
        String a = actual.toUpperCase();

        return ("EASY".equals(t) && "MEDIUM".equals(a))
                || ("MEDIUM".equals(t) && ("EASY".equals(a) || "HARD".equals(a)))
                || ("HARD".equals(t) && "MEDIUM".equals(a));
    }

    /**
     * Maps an Arc entity to an ArcResponse DTO.
     */
    private ArcResponse toArcResponse(Arc arc) {
        List<ArcMilestone> milestones = arcMilestoneRepository.findByArcIdOrderByOrderIndex(arc.getId());

        ArcType type;
        try {
            type = ArcType.valueOf(arc.getType().toUpperCase());
        } catch (Exception e) {
            type = ArcType.CUSTOM;
        }

        return ArcResponse.builder()
                .id(arc.getId())
                .name(arc.getName())
                .description(arc.getDescription())
                .type(type)
                .difficulty(arc.getDifficulty())
                .durationDays(arc.getDurationDays())
                .isPrebuilt(arc.getPrebuilt())
                .milestoneCount(milestones.size())
                .build();
    }

    /**
     * Internal record for scoring arcs during recommendation.
     */
    private record ScoredArc(Arc arc, int score) {}
}
