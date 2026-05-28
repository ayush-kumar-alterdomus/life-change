package com.ascend.quest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyQuestsResponse {

    private LocalDate date;
    private List<QuestResponse> quests;
    private int totalQuests;
    private int completedQuests;
}
