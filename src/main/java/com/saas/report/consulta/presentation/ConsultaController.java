package com.saas.report.consulta.presentation;

import com.saas.report.consulta.application.dto.ConsultaRequest;
import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.application.dto.ConvertirConsultaRequest;
import com.saas.report.consulta.application.usecase.ConvertirConsultaUseCase;
import com.saas.report.consulta.application.usecase.GetConsultaUseCase;
import com.saas.report.consulta.application.usecase.ListConsultasUseCase;
import com.saas.report.consulta.application.usecase.RegisterConsultaUseCase;
import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
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

import java.util.List;
import java.util.UUID;

/**
 * Registro y consulta del historial de consultas de reportes.
 *
 * <p>El historial pertenece a un tenant, que se toma del cuerpo en el registro y
 * del parámetro {@code tenantId} en el listado. Las rutas requieren autenticación.
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
     * @param request datos del reporte a convertir
     * @return la consulta ya convertida y registrada en el historial
     */
    @PostMapping("/convertir")
    public ResponseEntity<ConsultaResponse> convertir(@Valid @RequestBody ConvertirConsultaRequest request) {
        return ResponseEntity.ok(convertirConsultaUseCase.convertir(request));
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
     * Lista el historial de consultas de un tenant.
     *
     * @param tenantId tenant cuyas consultas se quieren listar
     * @return la lista de consultas del tenant
     */
    @GetMapping
    public ResponseEntity<List<ConsultaResponse>> historial(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(listConsultasUseCase.listar(new TenantUUID(tenantId)));
    }

    }