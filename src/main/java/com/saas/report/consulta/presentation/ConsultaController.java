package com.saas.report.consulta.presentation;

import com.saas.report.consulta.application.dto.ConsultaRequest;
import com.saas.report.consulta.application.dto.ConsultaResponse;
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

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final RegisterConsultaUseCase registerConsultaUseCase;
    private final ListConsultasUseCase listConsultasUseCase;
    private final GetConsultaUseCase getConsultaUseCase;

    public ConsultaController(RegisterConsultaUseCase registerConsultaUseCase,
                              ListConsultasUseCase listConsultasUseCase,
                              GetConsultaUseCase getConsultaUseCase) {
        this.registerConsultaUseCase = registerConsultaUseCase;
        this.listConsultasUseCase = listConsultasUseCase;
        this.getConsultaUseCase = getConsultaUseCase;
    }

    @PostMapping
    public ResponseEntity<ConsultaResponse> registrar(@Valid @RequestBody ConsultaRequest request) {
        ConsultaResponse response = registerConsultaUseCase.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{consultaId}")
    public ResponseEntity<ConsultaResponse> get(@PathVariable UUID consultaId) {
        return ResponseEntity.ok(getConsultaUseCase.obtener(new ConsultaHistorialUUID(consultaId)));
    }

    @GetMapping
    public ResponseEntity<List<ConsultaResponse>> historial(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(listConsultasUseCase.listar(new TenantUUID(tenantId)));
    }
}