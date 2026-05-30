package com.ascend.reward.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.reward.dto.*;
import com.ascend.reward.entity.Cosmetic;
import com.ascend.reward.repository.CosmeticRepository;
import com.ascend.reward.repository.UserCosmeticRepository;
import com.ascend.reward.service.AchievementService;
import com.ascend.reward.service.CurrencyService;
import com.ascend.reward.service.LootChestService;
import com.ascend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final CurrencyService currencyService;
    private final LootChestService lootChestService;
    private final AchievementService achievementService;
    private final CosmeticRepository cosmeticRepository;
    private final UserCosmeticRepository userCosmeticRepository;
    private final AuthService authService;

    @GetMapping("/currency")
    public ResponseEntity<ApiResponse<CurrencyResponse>> getBalance(
            @AuthenticationPrincipal FirebasePrincipal principal) {
        User user = authService.getCurrentUser(principal.uid());
        return ResponseEntity.ok(ApiResponse.success(currencyService.getBalance(user.getId())));
    }

    @GetMapping("/cosmetics")
    public ResponseEntity<ApiResponse<List<CosmeticResponse>>> getOwnedCosmetics(
            @AuthenticationPrincipal FirebasePrincipal principal) {
        User user = authService.getCurrentUser(principal.uid());
        List<CosmeticResponse> owned = cosmeticRepository.findAll().stream()
                .filter(c -> userCosmeticRepository.existsByUserIdAndCosmeticId(user.getId(), c.getId()))
                .map(c -> new CosmeticResponse(c.getId(), c.getName(), c.getType(), c.getRarity(), true, false))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(owned));
    }

    @GetMapping("/shop")
    public ResponseEntity<ApiResponse<List<CosmeticResponse>>> getShop(
            @AuthenticationPrincipal FirebasePrincipal principal) {
        User user = authService.getCurrentUser(principal.uid());
        List<CosmeticResponse> shop = cosmeticRepository.findAll().stream()
                .map(c -> new CosmeticResponse(c.getId(), c.getName(), c.getType(), c.getRarity(),
                        userCosmeticRepository.existsByUserIdAndCosmeticId(user.getId(), c.getId()), false))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(shop));
    }

    @PostMapping("/shop/buy")
    public ResponseEntity<ApiResponse<Void>> buyCosmetic(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestBody Map<String, UUID> body) {
        User user = authService.getCurrentUser(principal.uid());
        UUID cosmeticId = body.get("cosmeticId");
        Cosmetic cosmetic = cosmeticRepository.findById(cosmeticId).orElseThrow();

        if (cosmetic.getCoinCost() != null) {
            currencyService.spendCoins(user.getId(), cosmetic.getCoinCost());
        } else if (cosmetic.getGemCost() != null) {
            currencyService.spendGems(user.getId(), cosmetic.getGemCost());
        }

        return ResponseEntity.ok(ApiResponse.success("Cosmetic purchased"));
    }

    @GetMapping("/chests")
    public ResponseEntity<ApiResponse<List<LootChestResponse>>> getChests(
            @AuthenticationPrincipal FirebasePrincipal principal) {
        User user = authService.getCurrentUser(principal.uid());
        return ResponseEntity.ok(ApiResponse.success(lootChestService.getUnopenedChests(user.getId())));
    }

    @PostMapping("/chests/{id}/open")
    public ResponseEntity<ApiResponse<ChestOpenResult>> openChest(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @PathVariable UUID id) {
        User user = authService.getCurrentUser(principal.uid());
        ChestOpenResult result = lootChestService.openChest(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Chest opened", result));
    }

    @GetMapping("/achievements")
    public ResponseEntity<ApiResponse<List<AchievementResponse>>> getAchievements(
            @AuthenticationPrincipal FirebasePrincipal principal) {
        User user = authService.getCurrentUser(principal.uid());
        return ResponseEntity.ok(ApiResponse.success(achievementService.getAchievements(user.getId())));
    }
}
