package com.ascend.arc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ArcResponse {

    private UUID id;
    private String name;
    private String description;
    private ArcType type;
    private String difficulty;
    private Integer durationDays;
    private boolean isPrebuilt;
    private List<ArcPhase> phases;
    private Integer milestoneCount;
}
