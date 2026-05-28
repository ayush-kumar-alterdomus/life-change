package com.ascend.quest.exception;

import com.ascend.common.exception.BusinessException;

/**
 * Thrown when a user attempts to complete the same quest more than once per day.
 * Results in a 409 Conflict response.
 */
public class DuplicateCompletionException extends BusinessException {

    public DuplicateCompletionException(String message) {
        super("DUPLICATE_COMPLETION", message);
    }
}
