package com.saas.report.usuarios.presentation;

import com.saas.report.usuarios.application.usecase.VerificarEmailUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Verificación del email de un usuario recién registrado.
 *
 * <p>Consume el token que viaja en el enlace enviado por correo y confirma la
 * dirección. La ruta es pública porque se llega a ella desde el cliente sin
 * sesión iniciada.
 */
@RestController
@RequestMapping("/self-serve/verificar-email")
public class EmailVerificacionController {

    private final VerificarEmailUseCase verificarEmailUseCase;

    public EmailVerificacionController(VerificarEmailUseCase verificarEmailUseCase) {
        this.verificarEmailUseCase = verificarEmailUseCase;
    }

    /**
     * Confirma la dirección de email de un usuario.
     *
     * @param token token de verificación recibido por correo
     * @return respuesta vacía con estado {@code 200 OK}
     * @throws IllegalArgumentException si el token es inválido o ha expirado
     * @throws ResourceNotFoundException si el usuario del token no existe
     */
    @GetMapping
    public ResponseEntity<Void> verificar(@RequestParam("token") String token) {
        verificarEmailUseCase.verificar(token);
        return ResponseEntity.ok().build();
    }
}