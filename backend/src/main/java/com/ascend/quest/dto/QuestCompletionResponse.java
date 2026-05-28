package com.ascend.quest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO returned after a quest is successfully completed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestCompletionResponse {

    private UUID questId;
    private String questTitle;
    private int xpEarned;
    private LocalDateTime completedAt;
    private String message;
}
