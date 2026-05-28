package com.ascend.quest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteQuestRequest {

    @NotNull(message = "Quest ID is required")
    private UUID questId;
}
