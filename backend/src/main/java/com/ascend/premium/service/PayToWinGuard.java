package com.ascend.premium.service;

import com.ascend.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class PayToWinGuard {

    private static final Set<String> BLOCKED_ITEM_TYPES = Set.of(
            "XP_BOOST", "XP_PACK", "LEADERBOARD_RANK", "LEVEL_SKIP", "INSTANT_XP");

    public void validatePurchase(String itemType, UUID userId) {
        if (BLOCKED_ITEM_TYPES.contains(itemType.toUpperCase())) {
            log.warn("Pay-to-win purchase attempt blocked: user={} itemType={}", userId, itemType);
            throw new BusinessException("PAY_TO_WIN_BLOCKED",
                    "Purchases that directly grant XP or leaderboard rank are not allowed");
        }
    }
}
