package com.ascend.reward.dto;

import java.time.LocalDateTime;

public record AchievementResponse(String name, String type, String description, LocalDateTime unlockedAt, String badge) {}
