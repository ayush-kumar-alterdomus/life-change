package com.ascend.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * Rate limiting filter that enforces request limits per endpoint category.
 * Uses Redis sliding window counters keyed by user ID (authenticated) or IP (unauthenticated).
 * Returns HTTP 429 with Retry-After header when limits are exceeded.
 */
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final StringRedisTemplate redisTemplate;
    private final RateLimitConfig rateLimitConfig;

    public RateLimitFilter(StringRedisTemplate redisTemplate, RateLimitConfig rateLimitConfig) {
        this.redisTemplate = redisTemplate;
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!rateLimitConfig.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestPath = request.getRequestURI();
        RateLimitConfig.RateLimitBucket matchedBucket = findMatchingBucket(requestPath);

        if (matchedBucket == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = resolveClientKey(request);
        String redisKey = buildRedisKey(matchedBucket.getName(), clientKey);

        if (isRateLimited(redisKey, matchedBucket)) {
            log.warn("Rate limit exceeded: bucket={}, client={}, path={}",
                    matchedBucket.getName(), clientKey, requestPath);

            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(matchedBucket.getWindowSeconds()));
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Finds the first matching rate limit bucket for the given request path.
     * Buckets are evaluated in order, so more specific patterns should come first.
     */
    private RateLimitConfig.RateLimitBucket findMatchingBucket(String requestPath) {
        List<RateLimitConfig.RateLimitBucket> buckets = rateLimitConfig.getBuckets();
        for (RateLimitConfig.RateLimitBucket bucket : buckets) {
            if (PATH_MATCHER.match(bucket.getPathPattern(), requestPath)) {
                return bucket;
            }
        }
        return null;
    }

    /**
     * Resolves the client key for rate limiting.
     * Uses user ID if authenticated, otherwise falls back to client IP address.
     */
    private String resolveClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof FirebasePrincipal principal) {
            return "user:" + principal.uid();
        }

        return "ip:" + getClientIpAddress(request);
    }

    /**
     * Extracts the client IP address, considering X-Forwarded-For header for proxied requests.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // Take the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String buildRedisKey(String bucketName, String clientKey) {
        return RATE_LIMIT_KEY_PREFIX + bucketName + ":" + clientKey;
    }

    /**
     * Checks if the client has exceeded the rate limit using a Redis counter with expiry.
     * Uses INCR + EXPIRE pattern for a fixed window counter.
     * Returns true if rate limited, false if request is allowed.
     */
    private boolean isRateLimited(String redisKey, RateLimitConfig.RateLimitBucket bucket) {
        try {
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);

            if (currentCount == null) {
                return false;
            }

            // Set expiry on first request in the window
            if (currentCount == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(bucket.getWindowSeconds()));
            }

            return currentCount > bucket.getMaxRequests();
        } catch (Exception e) {
            // If Redis is unavailable, allow the request (fail open)
            log.error("Redis error during rate limit check, allowing request: {}", e.getMessage());
            return false;
        }
    }
}
