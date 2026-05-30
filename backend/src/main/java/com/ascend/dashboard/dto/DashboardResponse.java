package com.ascend.dashboard.dto;

import com.ascend.quest.dto.QuestResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record DashboardResponse(
        DashboardUserSection user,
        DashboardXpSection xp,
        DashboardStreakSection streak,
        DashboardDailyStatsSection dailyStats,
        List<QuestResponse> quests,
        DashboardArcSection activeArc,
        DashboardNotificationSection notifications
) {
}
