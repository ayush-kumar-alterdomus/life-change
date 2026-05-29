package com.ascend.xp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XpHistoryResponse {

    private List<XpTransaction> transactions;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class XpTransaction {

        private String source;
        private int amount;
        private BigDecimal multiplier;
        private String stat;
        private LocalDateTime timestamp;
    }
}
