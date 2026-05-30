package com.ascend.skilltree.exception;

import com.ascend.common.exception.BusinessException;

/**
 * Thrown when a premium user attempts to reset their skill tree
 * before the 30-day cooldown period has elapsed.
 * Results in a 409 Conflict response.
 */
public class SkillResetCooldownException extends BusinessException {

    public SkillResetCooldownException(String message) {
        super("SKILL_RESET_COOLDOWN", message);
    }
}
