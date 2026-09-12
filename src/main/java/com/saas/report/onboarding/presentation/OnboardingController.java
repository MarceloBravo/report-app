package com.saas.report.onboarding.presentation;

import com.saas.report.onboarding.application.dto.OnboardingRequest;
import com.saas.report.onboarding.application.dto.OnboardingResponse;
import com.saas.report.onboarding.application.usecase.OnboardClienteUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/self-serve")
public class OnboardingController {

    private final OnboardClienteUseCase onboardClienteUseCase;

    public OnboardingController(OnboardClienteUseCase onboardClienteUseCase) {
        this.onboardClienteUseCase = onboardClienteUseCase;
    }

    @PostMapping("/registro")
    public ResponseEntity<OnboardingResponse> registrar(@Valid @RequestBody OnboardingRequest request) {
        OnboardingResponse response = onboardClienteUseCase.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}