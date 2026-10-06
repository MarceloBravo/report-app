package com.saas.report.auth.application;

import com.saas.report.auth.application.dto.TokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthCookieServiceTest {

    private static final TokenResponse TOKENS =
            new TokenResponse("acc", "ref", "Bearer", 900_000, 604_800_000);

    @Test
    @DisplayName("Las cookies de sesión son HttpOnly con el path y la vigencia de cada token")
    void cookiesDeSesion() {
        List<String> cookies = new AuthCookieService(false, "Lax").cookiesDeSesion(TOKENS);

        assertThat(cookies).hasSize(2);
        assertThat(cookies.get(0))
                .startsWith("access_token=acc")
                .contains("Path=/", "Max-Age=900", "HttpOnly", "SameSite=Lax")
                .doesNotContain("Secure");
        assertThat(cookies.get(1))
                .startsWith("refresh_token=ref")
                .contains("Path=/auth", "Max-Age=604800", "HttpOnly", "SameSite=Lax");
    }

    @Test
    @DisplayName("En producción se marcan Secure y el SameSite configurado")
    void cookiesSeguras() {
        List<String> cookies = new AuthCookieService(true, "Strict").cookiesDeSesion(TOKENS);

        assertThat(cookies).allSatisfy(valor ->
                assertThat(valor).contains("Secure", "SameSite=Strict"));
    }

    @Test
    @DisplayName("Las cookies expiradas borran ambos tokens con Max-Age=0")
    void cookiesExpiradas() {
        List<String> cookies = new AuthCookieService(false, "Lax").cookiesExpiradas();

        assertThat(cookies).hasSize(2);
        assertThat(cookies.get(0)).startsWith("access_token=").contains("Max-Age=0", "Path=/");
        assertThat(cookies.get(1)).startsWith("refresh_token=").contains("Max-Age=0", "Path=/auth");
    }
}
