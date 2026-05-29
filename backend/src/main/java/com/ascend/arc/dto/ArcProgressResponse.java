package com.ascend.arc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.ascend.arc.entity.ArcStatus;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArcProgressResponse {

    private UUID arcId;
    private String arcName;
    private Integer progressPercent;
    private ArcPhase currentPhase;
    private LocalDateTime startedAt;
    private ArcStatus status;
    private Integer milestonesCompleted;
    private Integer totalMilestones;
}
