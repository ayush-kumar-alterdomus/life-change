package com.ascend.common.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Common Enums")
class EnumsTest {

    @Nested
    @DisplayName("Difficulty")
    class DifficultyTest {

        @Test
        @DisplayName("should have exactly 4 values")
        void shouldHaveFourValues() {
            assertThat(Difficulty.values()).hasSize(4);
        }

        @Test
        @DisplayName("should contain EASY, MEDIUM, HARD, LEGENDARY in order")
        void shouldContainExpectedValues() {
            assertThat(Difficulty.values()).containsExactly(
                    Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD, Difficulty.LEGENDARY
            );
        }

        @Test
        @DisplayName("should resolve from string via valueOf")
        void shouldResolveFromString() {
            assertThat(Difficulty.valueOf("EASY")).isEqualTo(Difficulty.EASY);
            assertThat(Difficulty.valueOf("LEGENDARY")).isEqualTo(Difficulty.LEGENDARY);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for invalid value")
        void shouldThrowForInvalidValue() {
            assertThatThrownBy(() -> Difficulty.valueOf("IMPOSSIBLE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Frequency")
    class FrequencyTest {

        @Test
        @DisplayName("should have exactly 4 values")
        void shouldHaveFourValues() {
            assertThat(Frequency.values()).hasSize(4);
        }

        @Test
        @DisplayName("should contain DAILY, WEEKLY, MONTHLY, ONE_TIME in order")
        void shouldContainExpectedValues() {
            assertThat(Frequency.values()).containsExactly(
                    Frequency.DAILY, Frequency.WEEKLY, Frequency.MONTHLY, Frequency.ONE_TIME
            );
        }

        @Test
        @DisplayName("should resolve from string via valueOf")
        void shouldResolveFromString() {
            assertThat(Frequency.valueOf("DAILY")).isEqualTo(Frequency.DAILY);
            assertThat(Frequency.valueOf("ONE_TIME")).isEqualTo(Frequency.ONE_TIME);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for invalid value")
        void shouldThrowForInvalidValue() {
            assertThatThrownBy(() -> Frequency.valueOf("YEARLY"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("StatType")
    class StatTypeTest {

        @Test
        @DisplayName("should have exactly 6 values")
        void shouldHaveSixValues() {
            assertThat(StatType.values()).hasSize(6);
        }

        @Test
        @DisplayName("should contain all stat types in order")
        void shouldContainExpectedValues() {
            assertThat(StatType.values()).containsExactly(
                    StatType.STRENGTH, StatType.WISDOM, StatType.FOCUS,
                    StatType.DISCIPLINE, StatType.VITALITY, StatType.CHARISMA
            );
        }

        @Test
        @DisplayName("should resolve from string via valueOf")
        void shouldResolveFromString() {
            assertThat(StatType.valueOf("STRENGTH")).isEqualTo(StatType.STRENGTH);
            assertThat(StatType.valueOf("CHARISMA")).isEqualTo(StatType.CHARISMA);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for invalid value")
        void shouldThrowForInvalidValue() {
            assertThatThrownBy(() -> StatType.valueOf("LUCK"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("all values should be uppercase (naming convention)")
        void allValuesShouldBeUppercase() {
            for (StatType stat : StatType.values()) {
                assertThat(stat.name()).isEqualTo(stat.name().toUpperCase());
            }
        }
    }
}
