package com.ascend.league.entity;

/**
 * Types of anti-cheat violations that can be detected.
 */
public enum ViolationType {

    /**
     * More than 10 quest completions in 5 minutes.
     */
    SPEED_VIOLATION,

    /**
     * More than 50 quest completions in 2 minutes.
     */
    BULK_SPAM
}
