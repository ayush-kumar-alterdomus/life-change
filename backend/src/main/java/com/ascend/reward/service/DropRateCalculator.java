package com.ascend.reward.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DropRateCalculator {

    private static final double BASE_RARE = 0.25;
    private static final double BASE_EPIC = 0.12;
    private static final double BASE_LEGENDARY = 0.03;

    /**
     * Calculates drop rates adjusted by streak bonus and event multiplier.
     * Higher bonuses shift probability toward rarer items.
     * Rates always sum to 1.0.
     */
    public Map<String, Double> calculateDropRates(String tier, double streakBonus, double eventMultiplier) {
        double multiplier = Math.max(1.0, streakBonus * eventMultiplier);

        double legendary = Math.min(0.20, BASE_LEGENDARY * multiplier);
        double epic = Math.min(0.30, BASE_EPIC * multiplier);
        double rare = Math.min(0.40, BASE_RARE * multiplier);
        double common = 1.0 - legendary - epic - rare;

        Map<String, Double> rates = new LinkedHashMap<>();
        rates.put("COMMON", common);
        rates.put("RARE", rare);
        rates.put("EPIC", epic);
        rates.put("LEGENDARY", legendary);
        return rates;
    }

    /**
     * Rolls a random rarity based on the given drop rates.
     */
    public String rollLoot(Map<String, Double> rates) {
        double roll = ThreadLocalRandom.current().nextDouble();
        double cumulative = 0.0;
        for (Map.Entry<String, Double> entry : rates.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                return entry.getKey();
            }
        }
        return "COMMON";
    }
}
