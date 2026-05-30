package com.ascend.reward.dto;

import java.util.UUID;

public record LootChestResponse(UUID id, String tier, String source, boolean opened, String contents) {}
