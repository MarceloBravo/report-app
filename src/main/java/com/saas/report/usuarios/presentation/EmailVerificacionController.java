package com.saas.report.usuarios.presentation;

import com.saas.report.usuarios.application.usecase.VerificarEmailUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/self-serve/verificar-email")
public class EmailVerificacionController {

    private final VerificarEmailUseCase verificarEmailUseCase;

    public EmailVerificacionController(VerificarEmailUseCase verificarEmailUseCase) {
        this.verificarEmailUseCase = verificarEmailUseCase;
    }

    @GetMapping
    public ResponseEntity<Void> verificar(@RequestParam("token") String token) {
        verificarEmailUseCase.verificar(token);
        return ResponseEntity.ok().build();
    }
}