package com.saas.report.support;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint mínimo autenticado para ejercitar los filtros de la cadena de
 * seguridad (CSRF por cabecera, JWT por header o cookie) en tests de slice,
 * sin depender de los controllers reales de cada módulo.
 */
@RestController
public class CsrfProbeController {

    public static final String RUTA = "/csrf-probe";

    @GetMapping(RUTA)
    public ResponseEntity<Void> get() {
        return ResponseEntity.ok().build();
    }

    @PostMapping(RUTA)
    public ResponseEntity<Void> post() {
        return ResponseEntity.ok().build();
    }
}
