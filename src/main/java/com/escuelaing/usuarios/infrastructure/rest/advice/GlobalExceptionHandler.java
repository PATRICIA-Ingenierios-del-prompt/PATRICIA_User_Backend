package com.escuelaing.usuarios.infrastructure.rest.advice;

import com.escuelaing.usuarios.domain.exception.DominioInvalidoException;
import com.escuelaing.usuarios.domain.exception.EstadoUsuarioInvalidoException;
import com.escuelaing.usuarios.domain.exception.FotoNoEncontradaException;
import com.escuelaing.usuarios.domain.exception.InteresInvalidoException;
import com.escuelaing.usuarios.domain.exception.MaxFotosException;
import com.escuelaing.usuarios.domain.exception.OnboardingException;
import com.escuelaing.usuarios.domain.exception.PerfilNoEncontradoException;
import com.escuelaing.usuarios.domain.exception.RolNoPermitidoException;
import com.escuelaing.usuarios.domain.exception.UsuarioNoEncontradoException;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones. Traduce las excepciones de dominio a
 * los códigos HTTP especificados en el contrato de usuarios-service:
 * - InteresInvalidoException  -> 400
 * - DominioInvalidoException  -> 400
 * - MaxFotosException         -> 409
 * - EstadoUsuarioInvalidoException -> 409
 * - OnboardingYaCompletadoException -> 409
 * - UsuarioNoEncontradoException / PerfilNoEncontradoException / FotoNoEncontradaException -> 404
 * - RolNoPermitidoException / AccessDeniedException -> 403
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InteresInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleInteresInvalido(InteresInvalidoException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "InteresInvalido", ex.getMessage());
    }

    @ExceptionHandler(DominioInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleDominioInvalido(DominioInvalidoException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "DominioInvalido", ex.getMessage());
    }

    @ExceptionHandler(MaxFotosException.class)
    public ResponseEntity<ErrorResponse> handleMaxFotos(MaxFotosException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "MaxFotosExcedido", ex.getMessage());
    }

    @ExceptionHandler(EstadoUsuarioInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleEstadoInvalido(EstadoUsuarioInvalidoException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "EstadoUsuarioInvalido", ex.getMessage());
    }

    @ExceptionHandler(OnboardingException.class)
    public ResponseEntity<ErrorResponse> handleOnboardingYaCompletado(OnboardingException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "OnboardingYaCompletado", ex.getMessage());
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "UsuarioNoEncontrado", ex.getMessage());
    }

    @ExceptionHandler(PerfilNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handlePerfilNoEncontrado(PerfilNoEncontradoException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "PerfilNoEncontrado", ex.getMessage());
    }

    @ExceptionHandler(FotoNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleFotoNoEncontrada(FotoNoEncontradaException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "FotoNoEncontrada", ex.getMessage());
    }

    @ExceptionHandler(RolNoPermitidoException.class)
    public ResponseEntity<ErrorResponse> handleRolNoPermitido(RolNoPermitidoException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "RolNoPermitido", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "AccesoDenegado", "No tiene permisos para realizar esta acción");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "ValidacionFallida", "Datos de entrada inválidos", detalles);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "ErrorInterno", "Ocurrió un error inesperado");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), error, message));
    }
}
