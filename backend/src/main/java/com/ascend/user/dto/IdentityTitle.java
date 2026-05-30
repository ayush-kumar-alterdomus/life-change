package com.ascend.user.dto;

import com.ascend.common.entity.StatType;

/**
 * Represents an identity title unlocked when a stat reaches a threshold.
 * Titles are permanent achievements that are never revoked.
 */
public record IdentityTitle(
        StatType statType,
        int threshold,
        String titleName,
        String description
) {
}
