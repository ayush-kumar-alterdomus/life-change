package com.ascend.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for rate limiting buckets per endpoint category.
 * Uses Redis-backed sliding window counters for distributed rate limiting.
 */
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
@Getter
@Setter
public class RateLimitConfig {

    private boolean enabled = true;

    private List<RateLimitBucket> buckets = List.of(
            new RateLimitBucket("auth", "/api/v1/auth/**", 10, 60),
            new RateLimitBucket("quest-completion", "/api/v1/quests/*/complete", 20, 60),
            new RateLimitBucket("guild-chat", "/api/v1/guilds/*/chat", 30, 60),
            new RateLimitBucket("general-api", "/api/v1/**", 100, 60)
    );

    /**
     * Defines a rate limit bucket with a name, path pattern, max requests, and window in seconds.
     */
    @Getter
    @Setter
    public static class RateLimitBucket {
        private String name;
        private String pathPattern;
        private int maxRequests;
        private int windowSeconds;

        public RateLimitBucket() {
        }

        public RateLimitBucket(String name, String pathPattern, int maxRequests, int windowSeconds) {
            this.name = name;
            this.pathPattern = pathPattern;
            this.maxRequests = maxRequests;
            this.windowSeconds = windowSeconds;
        }
    }
}
