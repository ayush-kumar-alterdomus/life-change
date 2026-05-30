package com.ascend.social.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountabilityPartnerResponse(
        UUID partnerId,
        String username,
        LocalDateTime pairedAt
) {
}
