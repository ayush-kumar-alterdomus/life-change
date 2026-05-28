package com.ascend.xp.event;

import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.StatType;
import com.ascend.quest.event.QuestCompletedEvent;
import com.ascend.xp.service.XpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class XpEventListenerTest {

    @Mock
    private XpService xpService;

    @InjectMocks
    private XpEventListener xpEventListener;

    @Test
    void handleQuestCompleted_callsAwardXp() {
        UUID userId = UUID.randomUUID();
        QuestCompletedEvent event = new QuestCompletedEvent(
                this, userId, UUID.randomUUID(), "Test Quest",
                Difficulty.MEDIUM, StatType.FOCUS, 50, LocalDateTime.now()
        );

        xpEventListener.handleQuestCompleted(event);

        verify(xpService).awardXp(userId, event);
    }

    @Test
    void handleQuestCompleted_onError_doesNotThrow() {
        UUID userId = UUID.randomUUID();
        QuestCompletedEvent event = new QuestCompletedEvent(
                this, userId, UUID.randomUUID(), "Test Quest",
                Difficulty.HARD, StatType.STRENGTH, 100, LocalDateTime.now()
        );

        doThrow(new RuntimeException("DB error")).when(xpService).awardXp(userId, event);

        // Should not throw — errors are caught and logged
        xpEventListener.handleQuestCompleted(event);

        verify(xpService).awardXp(userId, event);
    }
}
