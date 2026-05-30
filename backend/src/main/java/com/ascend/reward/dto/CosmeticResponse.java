package com.ascend.reward.dto;

import java.util.UUID;

public record CosmeticResponse(UUID id, String name, String type, String rarity, boolean owned, boolean equipped) {}
