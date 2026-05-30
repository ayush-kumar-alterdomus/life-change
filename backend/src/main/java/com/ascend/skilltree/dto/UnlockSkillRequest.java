package com.ascend.skilltree.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnlockSkillRequest {

    @NotNull(message = "Skill node ID is required")
    private UUID skillNodeId;
}
