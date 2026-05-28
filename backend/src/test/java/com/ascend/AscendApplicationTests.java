package com.ascend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AscendApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the main class can be instantiated without errors
        assertDoesNotThrow(() -> new AscendApplication());
    }
}
