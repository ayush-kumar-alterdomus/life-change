package com.ascend.reward.dto;

import java.util.List;

public record ChestOpenResult(List<CosmeticResponse> items, int coinsEarned, int gemsEarned) {}
