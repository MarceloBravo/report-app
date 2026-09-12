package com.saas.report.onboarding.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SelfServeRateLimiter {

    private final ConcurrentMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final double capacity;
    private final double refillPerSecond;

    public SelfServeRateLimiter(
            @Value("${app.onboarding.rate-limit.requests-per-minute:5}") int requestsPerMinute) {
        this.capacity = Math.max(1, requestsPerMinute);
        this.refillPerSecond = this.capacity / 60.0;
    }

    public boolean intentarConsumir(String key) {
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(capacity, refillPerSecond));
        return bucket.intentarConsumir();
    }

    private static final class TokenBucket {

        private final double capacity;
        private final double refillPerSecond;
        private double tokens;
        private double lastRefill;

        TokenBucket(double capacity, double refillPerSecond) {
            this.capacity = capacity;
            this.refillPerSecond = refillPerSecond;
            this.tokens = capacity;
            this.lastRefill = System.nanoTime();
        }

        synchronized boolean intentarConsumir() {
            long now = System.nanoTime();
            double elapsedSeconds = (now - lastRefill) / 1_000_000_000.0;
            lastRefill = now;
            tokens = Math.min(capacity, tokens + elapsedSeconds * refillPerSecond);
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }
    }
}