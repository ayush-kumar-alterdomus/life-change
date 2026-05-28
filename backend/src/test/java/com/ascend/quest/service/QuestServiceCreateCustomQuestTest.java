package com.ascend.quest.service;

import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.Frequency;
import com.ascend.common.entity.StatType;
import com.ascend.common.exception.BusinessException;
import com.ascend.premium.service.PremiumService;
import com.ascend.quest.dto.CreateQuestRequest;
import com.ascend.quest.dto.QuestResponse;
import com.ascend.quest.entity.Quest;
import com.ascend.quest.exception.CustomQuestLimitException;
import com.ascend.quest.repository.QuestCompletionRepository;
import com.ascend.quest.repository.QuestRepository;
import com.ascend.quest.validator.QuestValidator;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestServiceCreateCustomQuestTest {

    @Mock
    private QuestRepository questRepository;

    @Mock
    private QuestCompletionRepository questCompletionRepository;

    @Mock
    private QuestValidator questValidator;

    @Mock
    private PremiumService premiumService;

    @Mock
    private UserRepository userRepository;

    private QuestService questService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        questService = new QuestService(
                questRepository,
                questCompletionRepository,
                questValidator,
                premiumService,
                userRepository
        );

        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .build();
    }

    @Test
    void createCustomQuest_validRequest_createsAndReturnsQuest() {
        CreateQuestRequest request = new CreateQuestRequest(
                "Morning Run", "Run 5km every morning",
                Difficulty.MEDIUM, 50, StatType.VITALITY, Frequency.DAILY
        );

        when(questValidator.validate(request)).thenReturn(Collections.emptyList());
        when(premiumService.isPremiumUser(userId)).thenReturn(false);
        when(questRepository.countByCreatedBy_IdAndCustomTrue(userId)).thenReturn(2L);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questRepository.save(any(Quest.class))).thenAnswer(invocation -> {
            Quest q = invocation.getArgument(0);
            q.setId(UUID.randomUUID());
            return q;
        });

        QuestResponse response = questService.createCustomQuest(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Morning Run");
        assertThat(response.getXpReward()).isEqualTo(50);
        assertThat(response.getDifficulty()).isEqualTo(Difficulty.MEDIUM);
        assertThat(response.getStatType()).isEqualTo(StatType.VITALITY);
        assertThat(response.getFrequency()).isEqualTo(Frequency.DAILY);
        assertThat(response.isCustom()).isTrue();

        ArgumentCaptor<Quest> questCaptor = ArgumentCaptor.forClass(Quest.class);
        verify(questRepository).save(questCaptor.capture());
        Quest savedQuest = questCaptor.getValue();
        assertThat(savedQuest.isCustom()).isTrue();
        assertThat(savedQuest.getCreatedBy()).isEqualTo(user);
        assertThat(savedQuest.isRecurring()).isTrue();
    }

    @Test
    void createCustomQuest_validationFails_throwsBusinessException() {
        CreateQuestRequest request = new CreateQuestRequest(
                "", null, null, null, null, null
        );

        List<QuestValidator.ValidationError> errors = List.of(
                new QuestValidator.ValidationError("title", "Title must not be blank"),
                new QuestValidator.ValidationError("difficulty", "Difficulty is required")
        );
        when(questValidator.validate(request)).thenReturn(errors);

        assertThatThrownBy(() -> questService.createCustomQuest(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Title must not be blank")
                .hasMessageContaining("Difficulty is required");

        verify(questRepository, never()).save(any());
    }

    @Test
    void createCustomQuest_freeUserAtLimit_throwsCustomQuestLimitException() {
        CreateQuestRequest request = new CreateQuestRequest(
                "New Quest", "Description",
                Difficulty.EASY, 20, StatType.FOCUS, Frequency.DAILY
        );

        when(questValidator.validate(request)).thenReturn(Collections.emptyList());
        when(premiumService.isPremiumUser(userId)).thenReturn(false);
        when(questRepository.countByCreatedBy_IdAndCustomTrue(userId)).thenReturn(5L);

        assertThatThrownBy(() -> questService.createCustomQuest(userId, request))
                .isInstanceOf(CustomQuestLimitException.class)
                .hasMessageContaining("maximum of 5 custom quests");

        verify(questRepository, never()).save(any());
    }

    @Test
    void createCustomQuest_premiumUser_noLimitEnforced() {
        CreateQuestRequest request = new CreateQuestRequest(
                "Premium Quest", "Unlimited quests",
                Difficulty.HARD, 200, StatType.STRENGTH, Frequency.WEEKLY
        );

        when(questValidator.validate(request)).thenReturn(Collections.emptyList());
        when(premiumService.isPremiumUser(userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questRepository.save(any(Quest.class))).thenAnswer(invocation -> {
            Quest q = invocation.getArgument(0);
            q.setId(UUID.randomUUID());
            return q;
        });

        QuestResponse response = questService.createCustomQuest(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Premium Quest");
        // Premium user should never trigger count check
        verify(questRepository, never()).countByCreatedBy_IdAndCustomTrue(any());
    }

    @Test
    void createCustomQuest_nullFrequency_defaultsToDaily() {
        CreateQuestRequest request = new CreateQuestRequest(
                "No Frequency Quest", "Test default",
                Difficulty.EASY, 10, StatType.WISDOM, null
        );

        // QuestValidator allows null frequency in this scenario (default applied after validation)
        when(questValidator.validate(request)).thenReturn(Collections.emptyList());
        when(premiumService.isPremiumUser(userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questRepository.save(any(Quest.class))).thenAnswer(invocation -> {
            Quest q = invocation.getArgument(0);
            q.setId(UUID.randomUUID());
            return q;
        });

        QuestResponse response = questService.createCustomQuest(userId, request);

        assertThat(response.getFrequency()).isEqualTo(Frequency.DAILY);

        ArgumentCaptor<Quest> questCaptor = ArgumentCaptor.forClass(Quest.class);
        verify(questRepository).save(questCaptor.capture());
        assertThat(questCaptor.getValue().getFrequency()).isEqualTo(Frequency.DAILY);
        assertThat(questCaptor.getValue().isRecurring()).isTrue();
    }

    @Test
    void createCustomQuest_oneTimeFrequency_notRecurring() {
        CreateQuestRequest request = new CreateQuestRequest(
                "One Time Quest", "Do once",
                Difficulty.LEGENDARY, 300, StatType.DISCIPLINE, Frequency.ONE_TIME
        );

        when(questValidator.validate(request)).thenReturn(Collections.emptyList());
        when(premiumService.isPremiumUser(userId)).thenReturn(false);
        when(questRepository.countByCreatedBy_IdAndCustomTrue(userId)).thenReturn(0L);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questRepository.save(any(Quest.class))).thenAnswer(invocation -> {
            Quest q = invocation.getArgument(0);
            q.setId(UUID.randomUUID());
            return q;
        });

        questService.createCustomQuest(userId, request);

        ArgumentCaptor<Quest> questCaptor = ArgumentCaptor.forClass(Quest.class);
        verify(questRepository).save(questCaptor.capture());
        assertThat(questCaptor.getValue().isRecurring()).isFalse();
    }

    @Test
    void createCustomQuest_userNotFound_throwsBusinessException() {
        CreateQuestRequest request = new CreateQuestRequest(
                "Quest", "Desc",
                Difficulty.EASY, 10, StatType.FOCUS, Frequency.DAILY
        );

        when(questValidator.validate(request)).thenReturn(Collections.emptyList());
        when(premiumService.isPremiumUser(userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questService.createCustomQuest(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User not found");

        verify(questRepository, never()).save(any());
    }
}
