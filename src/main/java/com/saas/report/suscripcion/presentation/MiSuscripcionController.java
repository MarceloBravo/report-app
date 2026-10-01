package com.saas.report.suscripcion.presentation;

import com.saas.report.shared.presentation.rest.SecurityContextAuth;
import com.saas.report.suscripcion.application.dto.MiSuscripcionResponse;
import com.saas.report.suscripcion.application.usecase.GetMiSuscripcionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consulta de la suscripción del tenant autenticado.
 *
 * <p>A diferencia de {@link SuscripcionController}, que opera sobre cualquier
 * suscripción desde el panel, esta ruta no recibe identificadores: el tenant se
 * resuelve del contexto de seguridad. Por eso un tenant solo puede ver la suya.
 */
@RestController
@RequestMapping("/suscripcion")
public class MiSuscripcionController {

    private final GetMiSuscripcionUseCase getMiSuscripcionUseCase;

    public MiSuscripcionController(GetMiSuscripcionUseCase getMiSuscripcionUseCase) {
        this.getMiSuscripcionUseCase = getMiSuscripcionUseCase;
    }

    /**
     * Devuelve la suscripción del tenant autenticado.
     *
     * @return la suscripción con su plan y su historial de precios
     * @throws ResourceNotFoundException si el tenant todavía no tiene suscripción
     */
    @GetMapping("/mia")
    public ResponseEntity<MiSuscripcionResponse> mia() {
        return ResponseEntity.ok(getMiSuscripcionUseCase.obtenerMiSuscripcion(SecurityContextAuth.tenantIdActual()));
    }
}