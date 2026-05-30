package com.ascend.reward.property;

import com.ascend.reward.service.DropRateCalculator;
import net.jqwik.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property 46: Drop rates sum to 1.0 for all valid inputs.
 */
class DropRatePropertyTest {

    private final DropRateCalculator calculator = new DropRateCalculator();

    @Property(tries = 100)
    void dropRatesAlwaysSumToOne(
            @ForAll("tiers") String tier,
            @ForAll("bonuses") double streakBonus,
            @ForAll("multipliers") double eventMultiplier) {

        Map<String, Double> rates = calculator.calculateDropRates(tier, streakBonus, eventMultiplier);

        double sum = rates.values().stream().mapToDouble(Double::doubleValue).sum();

        assertThat(sum).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.0001));
        rates.values().forEach(rate -> assertThat(rate).isGreaterThanOrEqualTo(0.0));
    }

    @Provide
    Arbitrary<String> tiers() {
        return Arbitraries.of("COMMON", "RARE", "EPIC", "LEGENDARY");
    }

    @Provide
    Arbitrary<Double> bonuses() {
        return Arbitraries.doubles().between(1.0, 5.0);
    }

    @Provide
    Arbitrary<Double> multipliers() {
        return Arbitraries.doubles().between(1.0, 3.0);
    }
}
