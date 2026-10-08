package com.saas.report.consulta.presentation;

import com.saas.report.shared.presentation.dto.PageResponse;
import com.saas.report.consulta.application.dto.ConsultaRequest;
import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.application.dto.ConvertirConsultaRequest;
import com.saas.report.consulta.application.usecase.ConvertirConsultaUseCase;
import com.saas.report.consulta.application.usecase.GetConsultaUseCase;
import com.saas.report.consulta.application.usecase.ListConsultasUseCase;
import com.saas.report.consulta.application.usecase.RegisterConsultaUseCase;
import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.shared.exception.ForbiddenException;
import com.saas.report.shared.presentation.rest.SecurityContextAuth;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Registro y consulta del historial de consultas de reportes.
 *
 * <p>El historial pertenece a un tenant, que se toma del cuerpo en el registro,
 * del parámetro {@code tenantId} en el listado y del contexto de seguridad en la
 * conversión. Ese listado es paginado con los
 * query params {@code page} y {@code size}. Las rutas requieren autenticación.
 */
@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final RegisterConsultaUseCase registerConsultaUseCase;
    private final ListConsultasUseCase listConsultasUseCase;
    private final GetConsultaUseCase getConsultaUseCase;
    private final ConvertirConsultaUseCase convertirConsultaUseCase;

    public ConsultaController(RegisterConsultaUseCase registerConsultaUseCase,
                              ListConsultasUseCase listConsultasUseCase,
                              GetConsultaUseCase getConsultaUseCase,
                              ConvertirConsultaUseCase convertirConsultaUseCase) {
        this.registerConsultaUseCase = registerConsultaUseCase;
        this.listConsultasUseCase = listConsultasUseCase;
        this.getConsultaUseCase = getConsultaUseCase;
        this.convertirConsultaUseCase = convertirConsultaUseCase;
    }

    /**
     * Convierte un reporte externo al formato interno de la plataforma.
     *
     * <p>El tenant y el usuario se toman del contexto de seguridad, no del cuerpo
     * de la petición.
     *
     * @param request datos del reporte a convertir
     * @return la consulta ya convertida y registrada en el historial
     */
    @PostMapping("/convertir")
    public ResponseEntity<ConsultaResponse> convertir(@Valid @RequestBody ConvertirConsultaRequest request) {
        TenantUUID tenantId = SecurityContextAuth.tenantIdActual();
        UUID usuarioId = usuarioIdActual();
        return ResponseEntity.ok(convertirConsultaUseCase.convertir(tenantId.getValue(), usuarioId, request));
    }

    private UUID usuarioIdActual() {
        String usuarioId = SecurityContextAuth.principalActual().usuarioId();
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new ForbiddenException("El usuario autenticado no tiene identificador");
        }
        try {
            return UUID.fromString(usuarioId);
        } catch (IllegalArgumentException ex) {
            throw new ForbiddenException("El usuario autenticado no es válido");
        }
    }

    /**
     * Registra una consulta en el historial del tenant.
     *
     * @param request datos de la consulta
     * @return la consulta registrada con estado {@code 201 CREATED}
     */
    @PostMapping
    public ResponseEntity<ConsultaResponse> registrar(@Valid @RequestBody ConsultaRequest request) {
        ConsultaResponse response = registerConsultaUseCase.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene una consulta del historial por su identificador.
     *
     * @param consultaId identificador de la consulta
     * @return la consulta encontrada
     * @throws ResourceNotFoundException si la consulta no existe
     */
    @GetMapping("/{consultaId}")
    public ResponseEntity<ConsultaResponse> get(@PathVariable UUID consultaId) {
        return ResponseEntity.ok(getConsultaUseCase.obtener(new ConsultaHistorialUUID(consultaId)));
    }

    /**
     * Lista el historial de consultas de un tenant, de la más reciente a la más antigua.
     *
     * @param tenantId tenant cuyas consultas se quieren listar
     * @param page     número de página, empezando en 0; null o negativo se trata como 0
     * @param size     cantidad de elementos por página; null o no positivo usa 10 y
     *                 el máximo es 200
     * @return la página de consultas del tenant
     */
    @GetMapping
    public ResponseEntity<PageResponse<ConsultaResponse>> historial(@RequestParam UUID tenantId,
                                                                     @RequestParam(required = false) Integer page,
                                                                     @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(
                PageResponse.from(listConsultasUseCase.listar(new TenantUUID(tenantId), page, size)));
    }
}