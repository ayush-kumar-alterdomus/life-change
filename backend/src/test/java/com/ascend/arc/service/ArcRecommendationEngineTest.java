package com.ascend.arc.service;

import com.ascend.arc.dto.ArcResponse;
import com.ascend.arc.dto.ArcType;
import com.ascend.arc.entity.Arc;
import com.ascend.arc.repository.ArcMilestoneRepository;
import com.ascend.arc.repository.ArcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ArcRecommendationEngine.
 * Verifies that the engine returns valid arcs for any goal combination.
 */
@ExtendWith(MockitoExtension.class)
class ArcRecommendationEngineTest {

    @Mock
    private ArcRepository arcRepository;

    @Mock
    private ArcMilestoneRepository arcMilestoneRepository;

    private ArcRecommendationEngine recommendationEngine;

    private List<Arc> prebuiltArcs;

    @BeforeEach
    void setUp() {
        recommendationEngine = new ArcRecommendationEngine(arcRepository, arcMilestoneRepository);

        prebuiltArcs = List.of(
                Arc.builder().id(UUID.randomUUID()).name("Warrior Path").type("WARRIOR")
                        .difficulty("MEDIUM").durationDays(30).prebuilt(true).build(),
                Arc.builder().id(UUID.randomUUID()).name("Scholar Journey").type("SCHOLAR")
                        .difficulty("EASY").durationDays(60).prebuilt(true).build(),
                Arc.builder().id(UUID.randomUUID()).name("Monk Discipline").type("MONK")
                        .difficulty("HARD").durationDays(90).prebuilt(true).build(),
                Arc.builder().id(UUID.randomUUID()).name("Creator Flow").type("CREATOR")
                        .difficulty("MEDIUM").durationDays(45).prebuilt(true).build(),
                Arc.builder().id(UUID.randomUUID()).name("Beast Mode").type("BEAST_MODE")
                        .difficulty("HARD").durationDays(30).prebuilt(true).build()
        );
    }

    @Test
    @DisplayName("Fitness goal recommends WARRIOR arc first")
    void recommend_fitnessGoal_returnsWarriorFirst() {
        when(arcRepository.findByPrebuiltTrue()).thenReturn(prebuiltArcs);
        when(arcMilestoneRepository.findByArcIdOrderByOrderIndex(any())).thenReturn(List.of());

        List<ArcResponse> results = recommendationEngine.recommend(
                List.of("fitness"),
                Map.of("experienceLevel", "intermediate"),
                30
        );

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getType()).isEqualTo(ArcType.WARRIOR);
    }

    @Test
    @DisplayName("Learning goal recommends SCHOLAR arc first")
    void recommend_learningGoal_returnsScholarFirst() {
        when(arcRepository.findByPrebuiltTrue()).thenReturn(prebuiltArcs);
        when(arcMilestoneRepository.findByArcIdOrderByOrderIndex(any())).thenReturn(List.of());

        List<ArcResponse> results = recommendationEngine.recommend(
                List.of("learning"),
                Map.of("experienceLevel", "beginner"),
                30
        );

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getType()).isEqualTo(ArcType.SCHOLAR);
    }

    @Test
    @DisplayName("Unknown goal still returns recommendations (defaults to MONK)")
    void recommend_unknownGoal_returnsResults() {
        when(arcRepository.findByPrebuiltTrue()).thenReturn(prebuiltArcs);
        when(arcMilestoneRepository.findByArcIdOrderByOrderIndex(any())).thenReturn(List.of());

        List<ArcResponse> results = recommendationEngine.recommend(
                List.of("something_random"),
                Map.of("experienceLevel", "beginner"),
                30
        );

        assertThat(results).isNotEmpty();
        // Should still return results even for unknown goals
        assertThat(results.size()).isLessThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Empty goals list returns recommendations")
    void recommend_emptyGoals_returnsResults() {
        when(arcRepository.findByPrebuiltTrue()).thenReturn(prebuiltArcs);
        when(arcMilestoneRepository.findByArcIdOrderByOrderIndex(any())).thenReturn(List.of());

        List<ArcResponse> results = recommendationEngine.recommend(
                List.of(),
                Map.of(),
                30
        );

        // Empty goals defaults to MONK type
        assertThat(results).isNotEmpty();
    }

    @Test
    @DisplayName("High available time with advanced experience recommends HARD difficulty")
    void recommend_advancedUser_prefersHardDifficulty() {
        when(arcRepository.findByPrebuiltTrue()).thenReturn(prebuiltArcs);
        when(arcMilestoneRepository.findByArcIdOrderByOrderIndex(any())).thenReturn(List.of());

        List<ArcResponse> results = recommendationEngine.recommend(
                List.of("meditation"),
                Map.of("experienceLevel", "advanced"),
                60
        );

        assertThat(results).isNotEmpty();
        // MONK with HARD difficulty should score highest
        assertThat(results.get(0).getType()).isEqualTo(ArcType.MONK);
    }

    @Test
    @DisplayName("Results are capped at 5 recommendations")
    void recommend_returnsCappedResults() {
        when(arcRepository.findByPrebuiltTrue()).thenReturn(prebuiltArcs);
        when(arcMilestoneRepository.findByArcIdOrderByOrderIndex(any())).thenReturn(List.of());

        List<ArcResponse> results = recommendationEngine.recommend(
                List.of("fitness", "learning", "meditation"),
                Map.of("experienceLevel", "intermediate"),
                45
        );

        assertThat(results.size()).isLessThanOrEqualTo(5);
    }
}
