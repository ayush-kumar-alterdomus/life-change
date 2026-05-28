package com.ascend.common.config;

/**
 * CORS configuration has been moved to SecurityConfig to avoid conflicts
 * with Spring Security's CORS handling.
 *
 * @see com.ascend.auth.config.SecurityConfig#corsConfigurationSource()
 */
// Intentionally empty — CORS is configured in SecurityConfig
public class CorsConfig {
}
