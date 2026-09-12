package com.saas.report.onboarding.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SelfServeRateLimiterTest {

    @Test
    void permiteElValorDeRafagaYLuegoBloquea() {
        SelfServeRateLimiter limiter = new SelfServeRateLimiter(2);

        assertThat(limiter.intentarConsumir("203.0.113.1")).isTrue();
        assertThat(limiter.intentarConsumir("203.0.113.1")).isTrue();
        assertThat(limiter.intentarConsumir("203.0.113.1")).isFalse();
    }

    @Test
    void mantieneContadoresIndependientesPorIp() {
        SelfServeRateLimiter limiter = new SelfServeRateLimiter(1);

        assertThat(limiter.intentarConsumir("203.0.113.1")).isTrue();
        assertThat(limiter.intentarConsumir("203.0.113.1")).isFalse();
        assertThat(limiter.intentarConsumir("203.0.113.2")).isTrue();
    }
}