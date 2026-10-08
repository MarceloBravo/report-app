package com.saas.report.conexion.presentation;

import com.saas.report.shared.security.AuthPrincipal;
import com.saas.report.conexion.application.dto.ConexionDetailResponse;
import com.saas.report.conexion.application.dto.ConexionListResponse;
import com.saas.report.conexion.application.usecase.GetConexionUseCase;
import com.saas.report.conexion.application.usecase.ListConexionesUseCase;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.shared.exception.ForbiddenException;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.shared.presentation.rest.SecurityContextAuth;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Consulta de las conexiones a bases de datos del tenant autenticado.
 *
 * <p>Por defecto el tenant nunca llega por la petición: se resuelve del
 * contexto de seguridad con {@link SecurityContextAuth#tenantIdActual()}, de
 * modo que cada tenant solo puede ver sus propias conexiones.
 *
 * <p><b>Sobrecarga con {@code tenantId}:</b> ambos GET admiten además el
 * query param opcional {@code tenantId}, reservado a los roles de plataforma
 * ({@code PLATFORM_ADMIN} y {@code PLATFORM_USER}) para consultar las
 * conexiones de cualquier tenant. Si el parámetro no viene, se mantiene el
 * comportamiento actual (tenant del contexto de seguridad). Si viene, la
 * resolución ocurre en esta capa de presentación:
 *
 * <ul>
 *   <li>rol de tenant ({@code ADMIN}, {@code VIEWER}) →
 *       {@link ForbiddenException} (403) sin invocar el caso de uso;</li>
 *   <li>rol de plataforma → se usa el tenant indicado, validando que tenga
 *       formato UUID válido; si no lo tiene se responde 404 con
 *       {@link ResourceNotFoundException}, igual que el resto de identificadores
 *       de este controller.</li>
 * </ul>
 *
 * <p>Ambos endpoints exigen autenticación y están reservados a cualquier rol
 * de la plataforma o del tenant ({@code PLATFORM_ADMIN}, {@code PLATFORM_USER},
 * {@code ADMIN} y {@code VIEWER}), como indica la regla de
 * {@code /conexiones/**} de la configuración de seguridad; esa configuración
 * refuerza además que el parámetro {@code tenantId} solo lo puedan usar los
 * roles de plataforma.
 */
@RestController
@RequestMapping("/conexiones")
public class ConexionController {

    private static final String MENSAJE_TENANT_ID_SOLO_PLATAFORMA =
            "El parámetro tenantId solo puede usarse con roles de plataforma";

    private final ListConexionesUseCase listConexionesUseCase;
    private final GetConexionUseCase getConexionUseCase;

    public ConexionController(ListConexionesUseCase listConexionesUseCase,
                              GetConexionUseCase getConexionUseCase) {
        this.listConexionesUseCase = listConexionesUseCase;
        this.getConexionUseCase = getConexionUseCase;
    }

    /**
     * Lista las conexiones del tenant autenticado o, si el rol es de
     * plataforma, las del tenant indicado en {@code tenantId}.
     *
     * @param tenantId query param opcional con el tenant cuyas conexiones se
     *                 quieren listar; solo los roles de plataforma pueden usarlo
     * @return 200 con el resumen de cada conexión del tenant resuelto
     * @throws ForbiddenException si un rol de tenant envía el parámetro
     * @throws ResourceNotFoundException si el parámetro no es un UUID válido
     */
    @GetMapping
    public ResponseEntity<List<ConexionListResponse>> listar(
            @RequestParam(name = "tenantId", required = false) String tenantId) {
        return ResponseEntity.ok(listConexionesUseCase.listar(resolverTenant(tenantId)));
    }

    /**
     * Devuelve el detalle de una conexión concreta del tenant autenticado o,
     * si el rol es de plataforma, del tenant indicado en {@code tenantId}.
     *
     * @param dbConnectionId identificador de la conexión
     * @param tenantId       query param opcional con el tenant cuya conexión
     *                       consultar; solo los roles de plataforma pueden usarlo
     * @return 200 con el detalle de la conexión
     * @throws ResourceNotFoundException si el identificador no es una conexión
     *         válida, la conexión pertenece a otro tenant o {@code tenantId}
     *         no tiene formato UUID válido
     * @throws ForbiddenException si un rol de tenant envía {@code tenantId}
     */
    @GetMapping("/{dbConnectionId}")
    public ResponseEntity<ConexionDetailResponse> detalle(
            @PathVariable String dbConnectionId,
            @RequestParam(name = "tenantId", required = false) String tenantId) {
        return ResponseEntity.ok(getConexionUseCase.obtener(
                resolverTenant(tenantId),
                parsear(dbConnectionId)));
    }

    /**
     * Resuelve el tenant de la petición: sin parámetro, el del contexto de
     * seguridad; con parámetro, únicamente si el rol es de plataforma.
     */
    private TenantUUID resolverTenant(String tenantId) {
        if (tenantId == null) {
            return SecurityContextAuth.tenantIdActual();
        }
        exigirRolPlataforma();
        return parsearTenant(tenantId);
    }

    private void exigirRolPlataforma() {
        AuthPrincipal principal = SecurityContextAuth.principalActual();
        boolean esPlataforma;
        try {
            esPlataforma = UsuarioRol.valueOf(principal.rol()).esRolPlataforma();
        } catch (IllegalArgumentException | NullPointerException ex) {
            esPlataforma = false;
        }
        if (!esPlataforma) {
            throw new ForbiddenException(MENSAJE_TENANT_ID_SOLO_PLATAFORMA);
        }
    }

    private TenantUUID parsearTenant(String tenantId) {
        try {
            return new TenantUUID(UUID.fromString(tenantId));
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("No se encontró el tenant " + tenantId);
        }
    }

    private TenantDbConnectionUUID parsear(String dbConnectionId) {
        try {
            return new TenantDbConnectionUUID(UUID.fromString(dbConnectionId));
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("No se encontró la conexión " + dbConnectionId);
        }
    }
}
