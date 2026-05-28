package com.ascend.quest.entity;

/**
 * Categories for quest classification.
 */
public enum QuestCategory {
    MAIN_QUEST("Main Quest"),
    SIDE_QUEST("Side Quest"),
    DAILY_MISSION("Daily Mission"),
    WEEKLY_CHALLENGE("Weekly Challenge"),
    EPIC_QUEST("Epic Quest");

    private final String displayName;

    QuestCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
