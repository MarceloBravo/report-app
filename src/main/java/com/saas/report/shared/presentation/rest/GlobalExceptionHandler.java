package com.saas.report.shared.presentation.rest;

import com.saas.report.auth.domain.exception.InvalidCredentialsException;
import com.saas.report.auth.domain.exception.TokenRevokedException;
import com.saas.report.consulta.domain.exception.LimiteConsultasExcedidoException;
import com.saas.report.consulta.domain.exception.ProveedorIaNoDisponibleException;
import com.saas.report.plan.domain.exception.PlanNombreAlreadyExistsException;
import com.saas.report.shared.exception.DomainException;
import com.saas.report.shared.exception.ForbiddenException;
import com.saas.report.shared.presentation.dto.ErrorResponse;
import com.saas.report.suscripcion.domain.exception.SuscripcionAlreadyActiveException;
import com.saas.report.usuarios.domain.exception.EmailAlreadyExistsException;
import com.saas.report.shared.exception.InvalidStatusException;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.tenant.domain.exception.TenantSlugAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex,
                                                                  HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(TokenRevokedException.class)
    public ResponseEntity<ErrorResponse> handleTokenRevoked(TokenRevokedException ex,
                                                            HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException ex,
                                                           HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(TenantSlugAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleTenantSlugExists(TenantSlugAlreadyExistsException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

@ExceptionHandler(PlanNombreAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePlanNombreExists(PlanNombreAlreadyExistsException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(SuscripcionAlreadyActiveException.class)
    public ResponseEntity<ErrorResponse> handleSuscripcionAlreadyActive(SuscripcionAlreadyActiveException ex,
                                                                        HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(InvalidStatusException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex,
                                                         HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(LimiteConsultasExcedidoException.class)
    public ResponseEntity<ErrorResponse> handleLimiteConsultasExcedido(LimiteConsultasExcedidoException ex,
                                                                       HttpServletRequest request) {
        return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request);
    }

    @ExceptionHandler(ProveedorIaNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handleProveedorIaNoDisponible(ProveedorIaNoDisponibleException ex,
                                                                       HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex,
                                                      HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                               HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

@ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                           HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message != null ? message : status.getReasonPhrase(),
                request.getServletPath(),
                LocalDateTime.now());
        return ResponseEntity.status(status).body(response);
    }
}