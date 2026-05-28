package com.ascend.quest.exception;

import com.ascend.common.exception.BusinessException;

/**
 * Thrown when a free-tier user attempts to create more custom quests
 * than the allowed limit (max 5 for free users).
 * Results in a 403 Forbidden response.
 */
public class CustomQuestLimitException extends BusinessException {

    public CustomQuestLimitException(String message) {
        super("CUSTOM_QUEST_LIMIT_EXCEEDED", message);
    }
}
