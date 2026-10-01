package com.saas.report.tenant.presentation;

import com.saas.report.shared.presentation.dto.PageResponse;
import com.saas.report.tenant.application.dto.TenantResponse;
import com.saas.report.tenant.application.usecase.ActivateTenantUseCase;
import com.saas.report.tenant.application.usecase.CancelTenantUseCase;
import com.saas.report.tenant.application.usecase.GetTenantUseCase;
import com.saas.report.tenant.application.usecase.SuspendTenantUseCase;
import com.saas.report.tenant.application.usecase.ListTenantUseCase;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Administración de tenants de la plataforma.
 *
 * <p>Permite consultar el listado paginado, con o sin filtro de búsqueda, y
 * gestionar el ciclo de vida del tenant: activación, suspensión y cancelación.
 *
 * <p>El listado se expone paginado en la propia URL ({@code /{page}/{size}}) en
 * lugar de con parámetros de query. Todas las rutas requieren los roles de
 * plataforma {@code PLATFORM_ADMIN} o {@code PLATFORM_USER}.
 */
@RestController
@RequestMapping("/admin/tenants")
public class TenantController {

    private final GetTenantUseCase getTenantUseCase;
    private final ActivateTenantUseCase activateTenantUseCase;
    private final SuspendTenantUseCase suspendTenantUseCase;
    private final CancelTenantUseCase cancelTenantUseCase;
    private final ListTenantUseCase listTenantUseCase;

    public TenantController(GetTenantUseCase getTenantUseCase,
                            ActivateTenantUseCase activateTenantUseCase,
                            SuspendTenantUseCase suspendTenantUseCase,
                            CancelTenantUseCase cancelTenantUseCase,
                            ListTenantUseCase listTenantUseCase) {
        this.getTenantUseCase = getTenantUseCase;
        this.activateTenantUseCase = activateTenantUseCase;
        this.suspendTenantUseCase = suspendTenantUseCase;
        this.cancelTenantUseCase = cancelTenantUseCase;
        this.listTenantUseCase = listTenantUseCase;
    }

    /**
     * Lista los tenants paginados, sin filtro de búsqueda.
     *
     * @param page número de página, empezando en 0
     * @param size cantidad de elementos por página
     * @return la página de tenants solicitada
     */
    @GetMapping("/{page}/{size}")
    public ResponseEntity<PageResponse<TenantResponse>> list(
            @PathVariable Integer page,
            @PathVariable Integer size
    ) {
        return ResponseEntity.ok(PageResponse.from(listTenantUseCase.list(null, page, size)));
    }

    /**
     * Lista los tenants paginados aplicando un filtro de búsqueda de texto libre.
     *
     * @param searchTextString texto a contrastar contra los datos del tenant
     * @param page            número de página, empezando en 0
     * @param size            cantidad de elementos por página
     * @return la página de tenants que coinciden con el filtro
     */
    @GetMapping("/{page}/{size}/{searchTextString}")
    public ResponseEntity<PageResponse<TenantResponse>> list(
            @PathVariable String searchTextString,
            @PathVariable Integer page,
            @PathVariable Integer size
    ) {
        return ResponseEntity.ok(PageResponse.from(listTenantUseCase.list(searchTextString, page, size)));
    }

    /**
     * Obtiene un tenant por su identificador.
     *
     * @param tenantId identificador del tenant
     * @return el tenant encontrado
     * @throws ResourceNotFoundException si el tenant no existe
     */
    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantResponse> get(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(getTenantUseCase.obtener(new TenantUUID(tenantId)));
    }

    /**
     * Activa un tenant, devolviéndolo a un estado operativo.
     *
     * @param tenantId identificador del tenant
     * @return el tenant ya activo
     * @throws ResourceNotFoundException si el tenant no existe
     * @throws InvalidStatusException si el tenant no admite la activación
     */
    @PostMapping("/{tenantId}/activate")
    public ResponseEntity<TenantResponse> activate(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(activateTenantUseCase.activar(new TenantUUID(tenantId)));
    }

    /**
     * Suspende un tenant, pausing su acceso sin cancelar su suscripción.
     *
     * @param tenantId identificador del tenant
     * @return el tenant ya suspendido
     * @throws ResourceNotFoundException si el tenant no existe
     * @throws InvalidStatusException si el tenant no admite la suspensión
     */
    @PostMapping("/{tenantId}/suspend")
    public ResponseEntity<TenantResponse> suspend(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(suspendTenantUseCase.suspender(new TenantUUID(tenantId)));
    }

    /**
     * Cancela un tenant de forma definitiva.
     *
     * @param tenantId identificador del tenant
     * @return el tenant ya cancelado
     * @throws ResourceNotFoundException si el tenant no existe
     * @throws InvalidStatusException si el tenant no admite la cancelación
     */
    @PostMapping("/{tenantId}/cancel")
    public ResponseEntity<TenantResponse> cancel(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(cancelTenantUseCase.cancelar(new TenantUUID(tenantId)));
    }
}