package com.saas.report.auth.infrastructure.security;

import com.saas.report.auth.application.AuthCookieService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/**
 * Defensa CSRF por cabecera personalizada complementaria a {@code SameSite}.
 *
 * <p>Exige {@code X-Requested-With: XMLHttpRequest} en los métodos inseguros
 * (POST/PUT/PATCH/DELETE) solo cuando la request trae las cookies de sesión
 * {@code access_token}/{@code refresh_token}, más siempre en {@code /auth/login}
 * para cerrar el login CSRF. Un navegador no puede añadir una cabecera
 * personalizada en una request cross-site sin que el preflight CORS la apruebe,
 * y solo los orígenes listados en {@code app.cors.allowed-origins} son
 * autorizados con credenciales.
 *
 * <p>Los clientes sin cookies (curl, Postman, Swagger con {@code Authorization:
 * Bearer}) no cambian: no tienen credenciales ambientales que explotar.
 *
 * <p>Ante una violación responde con {@link RestAccessDeniedHandler}, igual que
 * el resto de denegaciones de la cadena: 403 con {@code ErrorResponse} JSON.
 */
public class CustomHeaderCsrfFilter extends OncePerRequestFilter {

    /** Cabecera que un formulario o un script cross-site no pueden fijar. */
    public static final String HEADER = "X-Requested-With";

    /** Valor exacto que debe llevar la cabecera. */
    public static final String VALOR = "XMLHttpRequest";

    private static final Set<String> METODOS_INSEGUROS = Set.of(
            HttpMethod.POST.name(), HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(), HttpMethod.DELETE.name());

    private static final String RUTA_LOGIN = "/auth/login";

    private final RestAccessDeniedHandler accessDeniedHandler;

    public CustomHeaderCsrfFilter(RestAccessDeniedHandler accessDeniedHandler) {
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !METODOS_INSEGUROS.contains(request.getMethod().toUpperCase(Locale.ROOT));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!exigeCabecera(request) || VALOR.equals(request.getHeader(HEADER))) {
            filterChain.doFilter(request, response);
            return;
        }
        accessDeniedHandler.handle(request, response, new AccessDeniedException(
                "Falta la cabecera " + HEADER + "=" + VALOR
                        + ": es obligatoria en requests con cookies de sesión"));
    }

    /**
     * Solo exige la cabecera cuando hay credenciales ambientales que un ataque
     * CSRF podría explotar: nuestras cookies de sesión. {@code /auth/login}
     * siempre la exige para cerrar el login CSRF (fijar la sesión de otro).
     */
    private boolean exigeCabecera(HttpServletRequest request) {
        return tieneCookiesDeSesion(request) || RUTA_LOGIN.equals(path(request));
    }

    private boolean tieneCookiesDeSesion(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }
        for (Cookie cookie : cookies) {
            if (AuthCookieService.ACCESS_COOKIE.equals(cookie.getName())
                    || AuthCookieService.REFRESH_COOKIE.equals(cookie.getName())) {
                return true;
            }
        }
        return false;
    }

    private String path(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path != null && !path.isEmpty()) {
            return path;
        }
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }
}
