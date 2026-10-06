package com.saas.report.auth.application;

import com.saas.report.auth.application.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Construye los valores {@code Set-Cookie} de la sesión.
 *
 * <p>Ambos tokens viajan en cookies {@code HttpOnly} para que el JavaScript del
 * frontend no pueda leerlos: {@code access_token} (Path=/, vigencia del access
 * token) y {@code refresh_token} (Path=/auth, limitado a los endpoints de
 * renovación y cierre de sesión).
 *
 * <p>Los atributos {@code Secure} y {@code SameSite} son configurables vía
 * {@code app.auth.cookies.*}: en desarrollo HTTP se usa {@code Secure=false} y
 * en producción conviene {@code Secure=true}.
 */
@Component
public class AuthCookieService {

    /** Cookie con el JWT de acceso; la lee el filtro JWT y {@code /auth/logout}. */
    public static final String ACCESS_COOKIE = "access_token";

    /** Cookie con el JWT de refresco; la leen {@code /auth/refresh} y {@code /auth/logout}. */
    public static final String REFRESH_COOKIE = "refresh_token";

    private static final String PATH_ACCESO = "/";
    private static final String PATH_REFRESH = "/auth";

    private final boolean secure;
    private final String sameSite;

    public AuthCookieService(@Value("${app.auth.cookies.secure:false}") boolean secure,
                             @Value("${app.auth.cookies.same-site:Lax}") String sameSite) {
        this.secure = secure;
        this.sameSite = sameSite;
    }

    /** Par de cookies de sesión con la vigencia de cada token (en milisegundos). */
    public List<String> cookiesDeSesion(TokenResponse tokens) {
        return List.of(
                cookie(ACCESS_COOKIE, tokens.accessToken(), PATH_ACCESO,
                        Duration.ofMillis(tokens.expiresIn())),
                cookie(REFRESH_COOKIE, tokens.refreshToken(), PATH_REFRESH,
                        Duration.ofMillis(tokens.refreshExpiresIn())));
    }

    /** Par de cookies con {@code Max-Age=0} para borrarlas en el navegador. */
    public List<String> cookiesExpiradas() {
        return List.of(
                cookie(ACCESS_COOKIE, "", PATH_ACCESO, Duration.ZERO),
                cookie(REFRESH_COOKIE, "", PATH_REFRESH, Duration.ZERO));
    }

    private String cookie(String nombre, String valor, String path, Duration maxAge) {
        return ResponseCookie.from(nombre, valor)
                .path(path)
                .maxAge(maxAge)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .build()
                .toString();
    }
}
