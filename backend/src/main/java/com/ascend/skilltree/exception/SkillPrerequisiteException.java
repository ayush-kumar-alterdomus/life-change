package com.ascend.skilltree.exception;

import com.ascend.common.exception.BusinessException;

/**
 * Thrown when a user attempts to unlock a skill node without having
 * unlocked the required parent node first.
 * Results in a 409 Conflict response.
 */
public class SkillPrerequisiteException extends BusinessException {

    public SkillPrerequisiteException(String message) {
        super("SKILL_PREREQUISITE_NOT_MET", message);
    }
}
