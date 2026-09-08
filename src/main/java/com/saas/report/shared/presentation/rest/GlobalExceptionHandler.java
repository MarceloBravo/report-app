package com.saas.report.shared.presentation.rest;

import com.saas.report.auth.domain.exception.EmailAlreadyExistsException;
import com.saas.report.auth.domain.exception.InvalidCredentialsException;
import com.saas.report.auth.domain.exception.TokenRevokedException;
import com.saas.report.shared.presentation.dto.ErrorResponse;
import com.saas.report.shared.exception.InvalidStatusException;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.tenant.domain.exception.TenantSlugAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, request);
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