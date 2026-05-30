package com.ascend.reward.service;

import com.ascend.common.exception.BusinessException;
import com.ascend.reward.dto.ChestOpenResult;
import com.ascend.reward.dto.LootChestResponse;
import com.ascend.reward.entity.LootChest;
import com.ascend.reward.repository.LootChestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LootChestService {

    private final LootChestRepository chestRepository;
    private final DropRateCalculator dropRateCalculator;
    private final CurrencyService currencyService;

    @Transactional
    public LootChest awardChest(UUID userId, String tier, String source) {
        LootChest chest = LootChest.builder()
                .userId(userId)
                .tier(tier)
                .source(source)
                .build();
        chest = chestRepository.save(chest);
        log.info("Chest awarded: id={} user={} tier={} source={}", chest.getId(), userId, tier, source);
        return chest;
    }

    @Transactional
    public ChestOpenResult openChest(UUID userId, UUID chestId) {
        LootChest chest = chestRepository.findById(chestId)
                .orElseThrow(() -> new BusinessException("CHEST_NOT_FOUND", "Chest not found"));

        if (!chest.getUserId().equals(userId)) {
            throw new BusinessException("UNAUTHORIZED", "Chest does not belong to user");
        }
        if (Boolean.TRUE.equals(chest.getOpened())) {
            throw new BusinessException("ALREADY_OPENED", "Chest already opened");
        }

        Map<String, Double> rates = dropRateCalculator.calculateDropRates(chest.getTier(), 1.0, 1.0);
        String rarity = dropRateCalculator.rollLoot(rates);

        int coinsEarned = calculateCoinReward(rarity);
        int gemsEarned = "LEGENDARY".equals(rarity) ? 5 : 0;

        currencyService.awardCoins(userId, coinsEarned, "chest_" + chest.getTier());
        if (gemsEarned > 0) {
            currencyService.awardGems(userId, gemsEarned, "chest_legendary");
        }

        chest.setOpened(true);
        chest.setOpenedAt(LocalDateTime.now());
        chest.setContents(String.format("{\"rarity\":\"%s\",\"coins\":%d,\"gems\":%d}", rarity, coinsEarned, gemsEarned));
        chestRepository.save(chest);

        return new ChestOpenResult(List.of(), coinsEarned, gemsEarned);
    }

    @Transactional(readOnly = true)
    public List<LootChestResponse> getUnopenedChests(UUID userId) {
        return chestRepository.findByUserIdAndOpenedFalse(userId).stream()
                .map(c -> new LootChestResponse(c.getId(), c.getTier(), c.getSource(), c.getOpened(), c.getContents()))
                .toList();
    }

    private int calculateCoinReward(String rarity) {
        return switch (rarity) {
            case "LEGENDARY" -> 100;
            case "EPIC" -> 50;
            case "RARE" -> 25;
            default -> 10;
        };
    }
}
